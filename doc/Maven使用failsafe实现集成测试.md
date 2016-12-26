# 集成测试

使用mock之后单元测试可以完全不依赖外界环境，比如database（一般使用hsqldb in memory db来实现database测试，mock db太麻烦了），
ftp server，web service或者其他的功能模块。Mock测试带来的问题就是各个类，模块之间的集成测试完全没有做，这个时候就需要集成测试。
单元测试maven有surefire插件实现自动化，集成测试则有failsafe plugin。

**Failsafe和maven结合，将整个集成测试分为4个阶段：**


- pre-integration-test 启动测试环境，比如通过容器插件cargo启动容器，加载应用
- integration-test 执行集成测试用例
- post-integration-test 清理测试环境
- verify 检测测试结果

其中pre/post integration test主要是负责启动应用和测试后停止应用，
一般通过容器插件，jetty plugin, tomcat plugin 或者高级点的cargo plugin来实现。
2和4歩则是由failsafe来实现。

**1，3歩的实现略.**
此外,之所以选择cargo是它可以加载命令行参数，这样就可以通过jacoco来统计测试覆盖率，tomcat plugin就无法做到这一点。

**2，4歩的failsafe配置如下.**
主要是找到test文件夹下所有"/ITCase.Java","/IT.java","**/*IT.java"测试用例。
如果测试用例有其他的特征，可以通过在configuration节点Include/exclude来实现。
具体可以参考failsafe的官方文档：https://maven.apache.org/surefire/maven-failsafe-plugin/

```
<pluginManagement>
    <plugins>
        <plugin>

            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>2.18.1</version>

            <executions>
                <execution>
                  <id>integration-test</id>
                  <goals>
                    <goal>integration-test</goal>
                  </goals>
                </execution>
                <execution>
                  <id>verify</id>
                  <goals>
                    <goal>verify</goal>
                  </goals>
                </execution>
            </executions>

        </plugin>
    </plugins>
</pluginManagement>
```
Note:集成测试用例如果和单元测试用例放在同一个项目里，
     必须在单元测试的surefire中exclude所有的集成测试用例。

