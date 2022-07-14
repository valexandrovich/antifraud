package ua.com.solidity.common.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.com.solidity.common.Utils;

import java.util.*;

@SuppressWarnings("unused")
public class LoggerWrapperFactory {
    static final String LOGGER_OPTIONS_PROPERTY = "otp-etl.logger.options";
    static Map<Class<? extends FlexibleLoggerWrapper>, Set<Class<? extends FlexibleLoggerWrapper>>> rules = new HashMap<>();
    static final Map<String, FlexibleLoggerWrapper> loggers = new HashMap<>();
    static final Set<Class<? extends FlexibleLoggerWrapper>> registered = new HashSet<>();
    static List<Class<? extends FlexibleLoggerWrapper>> wrappers = new ArrayList<>();
    static final Set<String> options = new HashSet<>();
    static boolean optionsAll = false;
    static boolean optionsInitialized = false;

    static {
        registerWrappers();
    }

    private LoggerWrapperFactory() {
        // nothing
    }

    private static boolean initOptionsByContext() {
        if (optionsInitialized) return true;
        if (Utils.checkApplicationContext()) {
            includeOptionsByString(Utils.getContextProperty(LOGGER_OPTIONS_PROPERTY, ""));
            return optionsInitialized = true;
        }
        return false;
    }

    public static void includeOptionsByString(String str) {
        includeOptions(str.split(","));
    }

    public static void includeOptions(String ... opts) {
        if (optionsAll) return;
        for (var opt : opts) {
            opt = opt.trim();
            if (opt.equals("*")) {
                optionsAll = true;
                options.clear();
                break;
            } else {
                options.add(opt);
            }
        }
    }

    public static boolean isOptionIncluded(String option) {
        return initOptionsByContext() && (optionsAll || options.contains(option.trim()));
    }

    private static void registerWrappers() {
        registerWrapper(NoTraceExceptionLoggerWrapper.class);
    }

    private static Map<Class<? extends FlexibleLoggerWrapper>, Set<Class<? extends FlexibleLoggerWrapper>>> copyRules() {
        Map<Class<? extends FlexibleLoggerWrapper>, Set<Class<? extends FlexibleLoggerWrapper>>> res = new HashMap<>();
        for (var item : rules.entrySet()) {
            res.put(item.getKey(), new HashSet<>(item.getValue()));
        }
        return res;
    }

    private static boolean addDependency(
            Map<Class<? extends FlexibleLoggerWrapper>, Set<Class<? extends FlexibleLoggerWrapper>>> rules,
            Class<? extends FlexibleLoggerWrapper> clazz,
            Class<? extends FlexibleLoggerWrapper> dependency,
            boolean forceRegister) {
        boolean modified = false;
        Set<Class<? extends FlexibleLoggerWrapper>> dependencies = rules.getOrDefault(clazz, null);
        if (dependencies == null) {
            if (forceRegister) {
                registered.add(clazz);
            }
            dependencies = new HashSet<>();
            rules.put(clazz, dependencies);
            modified = true;
        } else {
            dependencies = rules.get(clazz);
        }
        if (dependency != null && dependency != clazz) {
            modified |= dependencies.add(dependency);
        }
        return modified;
    }

    private static boolean checkWrappers(
            Map<Class<? extends FlexibleLoggerWrapper>, Set<Class<? extends FlexibleLoggerWrapper>>> rules) {
        for (int i = 0; i < wrappers.size(); ++i) {
            Class<? extends FlexibleLoggerWrapper> item = wrappers.get(i);
            Set<Class<? extends FlexibleLoggerWrapper>> dependencies = rules.get(item);
            if (dependencies != null) {
                for (var dep: dependencies) {
                    if (wrappers.indexOf(dep) > i) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static List<Class<? extends FlexibleLoggerWrapper>> rebuildWrappers(
        Map<Class<? extends FlexibleLoggerWrapper>, Set<Class<? extends FlexibleLoggerWrapper>>> rules) {
        List<Class<? extends FlexibleLoggerWrapper>> res = new ArrayList<>();
        Set<Class<? extends FlexibleLoggerWrapper>> handled = new HashSet<>();
        for (var item: rules.entrySet()) {
            if (cantRebuildItem(rules, res, handled, item.getKey())) {
                return Collections.emptyList();
            }
        }
        return res;
    }

    private static boolean cantRebuildItem(
            Map<Class<? extends FlexibleLoggerWrapper>, Set<Class<? extends FlexibleLoggerWrapper>>> rules,
            List<Class<? extends FlexibleLoggerWrapper>> wrappers,
            Set<Class<? extends FlexibleLoggerWrapper>> handled,
            Class<? extends FlexibleLoggerWrapper> clazz) {
        if (wrappers.contains(clazz)) return true;
        Set<Class<? extends FlexibleLoggerWrapper>> dependencies = rules.get(clazz);
        for (var item: dependencies) {
            if (!handled.contains(item) && !cantRebuildItem(rules, wrappers, handled, item)) {
                return true;
            }
        }

        if (registered.contains(clazz)) {
            wrappers.add(clazz);
        }
        handled.add(clazz);
        return false;
    }

    private static void rebuildLoggers() {
        for (var item : loggers.entrySet()) {
            item.getValue().rebuildBy(wrappers);
        }
    }

    public static void registerWrapper(Class<? extends FlexibleLoggerWrapper> clazz, Class<? extends FlexibleLoggerWrapper>[] before,
                                          Class<? extends FlexibleLoggerWrapper>[] after) {
        if (clazz == null ||
                (rules.containsKey(clazz) &&
                        (before == null || before.length == 0) &&
                        (after == null || after.length == 0)
                )
        ) {
            return;
        }

        Map<Class<? extends FlexibleLoggerWrapper>, Set<Class<? extends FlexibleLoggerWrapper>>> newRules =
                copyRules();

        boolean modified = addDependency(newRules, clazz, null, true);
        if (before != null) {
            for (var item: before) {
                modified |= addDependency(newRules, item, clazz, false);
            }
        }

        if (after != null) {
            for (var item: after) {
                modified |= addDependency(newRules, clazz, item, false);
            }
        }

        if (modified) {
            List<Class<? extends FlexibleLoggerWrapper>> newWrappers = rebuildWrappers(newRules);
            if (newWrappers.isEmpty()) {
                return;
            }
            wrappers = newWrappers;
            rules = newRules;
            rebuildLoggers();
        }
    }

    @SuppressWarnings("unused")
    public static void registerWrapperBefore(Class<? extends FlexibleLoggerWrapper> clazz, Class<? extends FlexibleLoggerWrapper>[] before) {
        registerWrapper(clazz, before, null);
    }

    @SuppressWarnings("unused")
    public static void registerWrapperAfter(Class<? extends FlexibleLoggerWrapper> clazz, Class<? extends FlexibleLoggerWrapper>[] after) {
        registerWrapper(clazz, null, after);
    }

    @SuppressWarnings("unused")
    public static void registerWrapper(Class<? extends FlexibleLoggerWrapper> clazz) {
        registerWrapper(clazz, null, null);
    }

    @SuppressWarnings("unused")
    public static Logger getLogger(String name) {
        Logger logger = loggers.getOrDefault(name, null);
        if (logger == null) {
            FlexibleLoggerWrapper wrapper = new FlexibleLoggerWrapper(LoggerFactory.getLogger(name), FlexibleLoggerWrapper.class.getName());
            if (!wrappers.isEmpty()) {
                wrapper.rebuildBy(wrappers);
            }
            loggers.put(name, wrapper);
            logger = wrapper;
        }
        return logger;
    }
}
