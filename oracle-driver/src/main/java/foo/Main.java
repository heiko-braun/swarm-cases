package foo;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.config.security.Flag;
import org.wildfly.swarm.config.security.SecurityDomain;
import org.wildfly.swarm.config.security.security_domain.ClassicAuthentication;
import org.wildfly.swarm.config.security.security_domain.authentication.LoginModule;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.security.SecurityFraction;

import java.util.HashMap;

/**
 * @author Heiko Braun
 * @since 28/11/2016
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Swarm swarm = new Swarm(args);

        swarm.fraction(datasourceWithOracle());
        swarm.fraction(SecurityFraction.defaultSecurityFraction().securityDomain(securityDomain()));

        swarm.start().deploy();
    }

    private static DatasourcesFraction datasourceWithOracle() {
        return new DatasourcesFraction().jdbcDriver("com.oracle", (d) -> {
            d.driverModuleName("com.oracle");
            d.driverClassName("oracle.jdbc.OracleDriver");
            d.xaDatasourceClass("oracle.jdbc.xa.OracleXADataSource");
        }).dataSource("DespesasDS", (ds) -> {
            ds.driverName("com.oracle");
            ds.connectionUrl("url");
            ds.userName("user");
            ds.password("pass");
        });
    }

    private static SecurityDomain securityDomain() {
        return new SecurityDomain("controle-despesas")
                .classicAuthentication(
                        new ClassicAuthentication()
                                .loginModule(
                                        new LoginModule("ldapModule")
                                                .code("org.jboss.security.auth.spi.LdapExtLoginModule")
                                                .flag(Flag.REQUIRED)
                                                .moduleOptions(new HashMap<Object, Object>() {{
                                                    put("password-stacking", "useFirstPass");
                                                    put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
                                                    put("defaultRole", "AUTENTICADO");
                                                }})
                                )
                );
    }

}
