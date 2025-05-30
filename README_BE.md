# 良信东阳光项目

## 启动

```bash
java -jar lazzen-hec-service-server-0.1.0-SNAPSHOT.jar
```

## 配置文件

```yaml
sipa.boot:
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
            # 这里改地址+端口
            url: jdbc:mysql://139.9.236.144:3306/lite_store?useUnicode=true&characterEncoding=utf8&useSSL=false&tinyInt1isBit=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
            # 这里改账号
            username: root
            # 这里改密码
            password: N@der2022+1
          slave0:
            type: com.alibaba.druid.pool.DruidDataSource
            driver-class-name: com.mysql.cj.jdbc.Driver
            # 这里改地址+端口
            url: jdbc:mysql://139.9.236.144:3306/lite_store?useUnicode=true&characterEncoding=utf8&useSSL=false&tinyInt1isBit=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
            # 这里改账号
            username: root
            # 这里改密码
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
            # 这里改地址+端口
            url: jdbc:mysql://139.9.236.144:3306/lite_nader4_smart_management?useUnicode=true&characterEncoding=utf8&useSSL=false&tinyInt1isBit=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
            # 这里改账号
            username: root
            # 这里改密码
            password: N@der2022+1
          slave0:
            type: com.alibaba.druid.pool.DruidDataSource
            driver-class-name: com.mysql.cj.jdbc.Driver
            # 这里改地址+端口
            url: jdbc:mysql://139.9.236.144:3306/lite_nader4_smart_management?useUnicode=true&characterEncoding=utf8&useSSL=false&tinyInt1isBit=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
            # 这里改账号
            username: root
            # 这里改密码
            password: N@der2022+1
        masterslave:
          load-balance-algorithm-type: round_robin
          name: ms
          master-data-source-name: master
          slave-data-source-names: slave0
```
