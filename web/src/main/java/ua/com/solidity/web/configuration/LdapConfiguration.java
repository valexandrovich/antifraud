package ua.com.solidity.web.configuration;

//@Configuration
//@PropertySource("classpath:application.properties")
//@RequiredArgsConstructor
//@EnableLdapRepositories(basePackages = "ua.com.solidity.ad.repositories")
public class LdapConfiguration {

//
//    @Value("${ldap.config.url}")
//    private String ldapUrl;
//
//    @Value("${ldap.config.user}")
//    private String ldapUser;
//
//    @Value("${ldap.config.password}")
//    private String ldapPassword;
//
//    @Value("${ldap.config.root}")
//    private String ldapRoot;
//
//
//    @Value("${ldap.config.userSearchFilter}")
//    private String ldapUserSearchFilter;
//
//    @Bean
//    public LdapContextSource ldapContextSource() {
//        LdapContextSource ldapContextSource = new LdapContextSource();
//        ldapContextSource.setUrl(ldapUrl);
//        ldapContextSource.setUserDn(ldapUser);
//        ldapContextSource.setPassword(ldapPassword);
//        return ldapContextSource;
//    }
//
//    @Bean
//    public FilterBasedLdapUserSearch userSearch() {
//        return new FilterBasedLdapUserSearch(ldapRoot, ldapUserSearchFilter, ldapContextSource());
//    }
//
//    @Bean
//    public LdapUserDetailsService ldapUserDetailsService() {
//        LdapUserDetailsService ldapUserDetailsService = new LdapUserDetailsService(userSearch());
//        ldapUserDetailsService.setUserDetailsMapper(ldapUserDetailsMapper());
//        return ldapUserDetailsService;
//    }
//
//    @Bean
//    public LdapUserDetailsMapper ldapUserDetailsMapper() {
//        return new LdapUserDetailsMapper() {
//            @Override
//            public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
//                Object[] groups = ctx.getObjectAttributes("memberOf");
//                Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
//                if (groups != null) {
//                    for (Object group : groups) {
//                        String groupCleaned = "ROLE_" + group.toString().replace("CN=", "").split(",")[0];
//                        roles.add(new SimpleGrantedAuthority(groupCleaned));
//                    }
//                }
//                return super.mapUserFromContext(ctx, username, roles);
//            }
//        };
//    }


}
