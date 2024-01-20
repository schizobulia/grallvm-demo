clear

javac HelloWorld.java

cd src/main/java/org/example/

# 
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000 \
    --add-modules jdk.internal.vm.ci \
    --add-exports jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED \
    --add-exports jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED \
    --add-exports jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED \
    --add-exports jdk.internal.vm.ci/jdk.vm.ci.services=ALL-UNNAMED \
    --add-exports jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED \
    --add-exports jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+EnableJVMCI \
    Main.java
