# 良信东阳光项目

## 环境

- Java8 292-452 其它java8版未测试
- maven 3.9.6 其它maven版本未测试

## 技术栈

- 框架：Spring Boot + Sipa boot

## 配置

```yaml
# 文件：application.yml
# 需要修改ip 端口 账号 密码
datasource:
  enabled: true
  names:
    - lite-store
    - smart-management
  dynamics:
    lite-store:
      datasource:
        names: master,slave0
        master:
          type: com.alibaba.druid.pool.DruidDataSource
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://139.9.236.144:3306/lite_store?useUnicode=true&characterEncoding=utf8&useSSL=false&tinyInt1isBit=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
          username: root
          password: N@der2022+1
        slave0:
          type: com.alibaba.druid.pool.DruidDataSource
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://139.9.236.144:3306/lite_store?useUnicode=true&characterEncoding=utf8&useSSL=false&tinyInt1isBit=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
          username: root
          password: N@der2022+1
      masterslave:
        load-balance-algorithm-type: round_robin
        name: ms
        master-data-source-name: master
        slave-data-source-names: slave0
    smart-management:
      datasource:
        names: master,slave0
        master:
          type: com.alibaba.druid.pool.DruidDataSource
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://139.9.236.144:3306/lite_nader4_smart_management?useUnicode=true&characterEncoding=utf8&useSSL=false&tinyInt1isBit=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
          username: root
          password: N@der2022+1
        slave0:
          type: com.alibaba.druid.pool.DruidDataSource
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://139.9.236.144:3306/lite_nader4_smart_management?useUnicode=true&characterEncoding=utf8&useSSL=false&tinyInt1isBit=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
          username: root
          password: N@der2022+1
      masterslave:
        load-balance-algorithm-type: round_robin
        name: ms
        master-data-source-name: master
        slave-data-source-names: slave0
```

## 编译

```bash
git clone https://github.com/YueChengMinJie/sipa-boot.git
cd sipa-boot-java8
mvn -U -e -B clean install -DskipTests

cd lazzen-hec-service
mvn -U -e -B clean package -DskipTests
```

## 启动

```bash
java -jar lazzen-hec-service-server-0.1.0-SNAPSHOT.jar
```
