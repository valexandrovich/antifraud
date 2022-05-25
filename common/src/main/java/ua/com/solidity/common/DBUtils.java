package ua.com.solidity.common;

import lombok.CustomLog;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;


@CustomLog
public class DBUtils {

    private DBUtils() {
        // nothing
    }

    private static String[] getUrlContextParts(String url) {
        int query = url.indexOf('?');
        int ref = url.lastIndexOf('#');
        if (ref < 0) {
            ref = url.length();
        }

        if (query < 0) {
            query = ref;
        }

        String[] res = new String[3];
        res[0] = query > 0 ? url.substring(0, query) : "";
        res[1] = query + 1 <= ref ? url.substring(query + 1, ref) : "";
        res[2] = ref + 1 <= url.length() ? url.substring(ref + 1) : "";
        return res;
    }

    public static Map<String, List<String>> createParams() {
        return new LinkedHashMap<>();
    }

    public static Map<String, List<String>> createParams(String query) {
        Map<String, List<String>> target = createParams();
        parseUrlParams(query, target);
        return target;
    }

    public static void changeParams(Map<String, List<String>> params, String name, String value, boolean removeAll, boolean merge) {
        List<String> values = params.getOrDefault(name, null);
        if (values == null) {
            values = new ArrayList<>();
            values.add(value);
            params.put(name, values);
        } else {
            if (removeAll) {
                values.clear();
            } else if (merge && values.contains(value)) {
                return;
            }
            values.add(value);
        }
    }

    public static void mergeParams(Map<String, List<String>> params, Map<String, List<String>> paramsToMerge, boolean replace) {
        for (var entry : paramsToMerge.entrySet()) {
            for (String value : entry.getValue()) {
                changeParams(params, entry.getKey(), value, replace, true);
            }
        }
    }

    public static void parseUrlParams(String query, Map<String, List<String>> target) {
        if (query != null && !query.isBlank()) {
            String[] params = query.split("&");
            for (String param : params) {
                int index = param.indexOf('=');
                if (index == -1) {
                    changeParams(target, param, null, false, false);
                } else {
                    changeParams(target, param.substring(0, index), param.substring(index + 1), false, false);
                }
            }
        }
    }

    public static void getUrlParams(String url, Map<String, List<String>> target) {
        String[] parts = getUrlContextParts(url);
        if (parts[0].isEmpty()) return;
        if (!parts[1].isEmpty()) {
            parseUrlParams(url, target);
        }
    }

    @SuppressWarnings("unused")
    public static Map<String, List<String>> getUrlParams(String url) {
        Map<String, List<String>> res = createParams();
        getUrlParams(url, res);
        return res;
    }

    public static String mergeURLParams(String url, Map<String, List<String>> paramsToMerge) {
        String[] parts = getUrlContextParts(url);
        if (parts[0].isBlank()) {
            return "";
        }
        if (paramsToMerge == null) return url;

        Map<String, List<String>> params = createParams(parts[1]);
        mergeParams(params, paramsToMerge, true);
        StringBuilder b = new StringBuilder();
        b.append(parts[0]).append("?");
        boolean firstParamAdded = false;
        for (var entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                if (firstParamAdded) {
                    b.append("&");
                } else firstParamAdded = true;
                b.append(entry.getKey());
                if (value != null) {
                    b.append("=").append(value);
                }
            }
        }
        if (!parts[2].isBlank()) {
            b.append("#").append(parts[2]);
        }
        return b.toString();
    }

    public static String mergeURLParams(String url, String advancedParams) {
        Map<String, List<String>> paramsToMerge = createParams(advancedParams);
        return mergeURLParams(url, paramsToMerge);
    }

    public static Connection createConnection(String dataSourcePrefix, String advancedParams) throws SQLException {
        if (Utils.checkApplicationContext()) {
            ApplicationContext context = Utils.getApplicationContext();
            Environment env = context.getEnvironment();
            String url = env.getProperty(dataSourcePrefix + ".url");
            String userName = env.getProperty(dataSourcePrefix + ".username", "");
            String password = context.getEnvironment().getProperty(dataSourcePrefix + ".password", "");

            if (url != null && (url = mergeURLParams(url, advancedParams)) != null) {
                return DriverManager.getConnection(url, userName, password);
            }
        }
        return null;
    }
}
