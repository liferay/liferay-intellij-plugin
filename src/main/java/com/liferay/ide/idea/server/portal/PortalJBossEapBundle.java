package com.liferay.ide.idea.server.portal;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.lang.JavaVersion;
import com.liferay.ide.idea.util.FileUtil;
import org.jetbrains.jps.model.java.JdkVersionDetector;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PortalJBossEapBundle extends PortalJBossBundle {
    public PortalJBossEapBundle(Path path) {
        super(path);
    }

    @Override
    public String[] getRuntimeStartVMArgs(Sdk sdk) {
        List<String> args = new ArrayList<>();

        args.add("-Dcom.sun.management.jmxremote");
        args.add("-Dcom.sun.management.jmxremote.authenticate=false");
        args.add("-Dcom.sun.management.jmxremote.port=" + getJmxRemotePort());
        args.add("-Dcom.sun.management.jmxremote.ssl=false");
        args.add("-Dorg.jboss.resolver.warning=true");
        args.add("-Djava.net.preferIPv4Stack=true");
        args.add("-Dsun.rmi.dgc.client.gcInterval=3600000");
        args.add("-Dsun.rmi.dgc.server.gcInterval=3600000");
        args.add("-Djboss.modules.system.pkgs=org.jboss.byteman");
        args.add("-Djava.awt.headless=true");
        args.add("-Dfile.encoding=UTF8");

        args.add("-server");
        args.add("-Djava.util.logging.manager=org.jboss.logmanager.LogManager");

        JdkVersionDetector jdkVersionDetector = JdkVersionDetector.getInstance();

        JdkVersionDetector.JdkVersionInfo jdkVersionInfo = jdkVersionDetector.detectJdkVersionInfo(sdk.getHomePath());

        if (jdkVersionInfo != null) {
            JavaVersion jdkVersion = jdkVersionInfo.version;
            JavaVersion jdk8Version = JavaVersion.compose(8);

            File wildflyCommonLib = getJbossLib(bundlePath, "/modules/system/layers/base/org/wildfly/common/main/");

            if (jdkVersion.compareTo(jdk8Version) <= 0) {
                args.add(
                        "-Xbootclasspath/p:\"" + bundlePath +
                                "/modules/system/layers/base/org/jboss/logmanager/main/jboss-logmanager-1.5.4.Final-redhat-" +
                                "1.jar\"");
                args.add(
                        "-Xbootclasspath/p:\"" + bundlePath +
                                "/modules/system/layers/base/org/jboss/log4j/logmanager/main/log4j-jboss-logmanager-1.1.1.Final-" +
                                "redhat-1.jar\"");

                if (Objects.nonNull(wildflyCommonLib)) {
                    args.add("-Xbootclasspath/p:\"" + wildflyCommonLib.getAbsolutePath() + "\"");
                }

                File jbossLogManagerLib = getJbossLib(bundlePath, "/modules/system/layers/base/org/jboss/logmanager/main/");

                if (Objects.nonNull(jbossLogManagerLib)) {
                    args.add("-Xbootclasspath/p:\"" + jbossLogManagerLib.getAbsolutePath() + "\"");
                }
            }else {
                if (Objects.nonNull(wildflyCommonLib)) {
                    args.add("-Xbootclasspath/a:\"" + wildflyCommonLib.getAbsolutePath() + "\"");
                }
                args.add("--add-modules java.se");
            }
        }

        args.add("-Djboss.modules.system.pkgs=org.jboss.logmanager");

        args.add("-Dorg.jboss.boot.log.file=\"" + FileUtil.pathAppend(bundlePath, "/standalone/log/boot.log") + "\"");
        args.add("-Dlogging.configuration=file:\"" + bundlePath + "/standalone/configuration/logging.properties\"");
        args.add("-Djboss.home.dir=\"" + bundlePath + "\"");
        args.add("-Djboss.bind.address.management=localhost");
        args.add("-Duser.timezone=GMT");
        args.add("-Dorg.jboss.logmanager.nocolor=true");

        return args.toArray(new String[0]);
    }
}
