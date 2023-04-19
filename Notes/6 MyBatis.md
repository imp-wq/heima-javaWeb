# MyBatis

[mybatis – MyBatis 3 | Getting started](https://mybatis.org/mybatis-3/getting-started.html)

MyBatis是一款持久层框架，用于简化JDBC开发。

* Java EE的三层架构：
  * 表现层：页面展示
  * 业务层：逻辑处理
  * 持久层：将数据保存到数据库
* 针对JDBC的缺点的优化：
  * 硬编码=>配置文件
    * 注册驱动，获取连接，列名
    * sql语句
  * 操作繁琐=>自动完成
    * 手动封装结果集

## 基本使用

* 通过maven导包，my-batis和mysql

* 核心配置文件mybatis-config.xml，依据官网提示写。

  ```xml
  <?xml version="1.0" encoding="UTF-8" ?>
  <!DOCTYPE configuration
          PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
          "https://mybatis.org/dtd/mybatis-3-config.dtd">
  <configuration>
      <environments default="development">
          <environment id="development">
              <transactionManager type="JDBC"/>
              <dataSource type="POOLED">
                  <!--数据库连接信息，驱动等-->
                  <property name="driver" value="com.mysql.jdbc.Driver"/>
                  <property name="url" value="jdbc:mysql:///mybatis?useSSL=false"/>
                  <property name="username" value="root"/>
                  <property name="password" value="mysql"/>
  
              </dataSource>
          </environment>
      </environments>
      <mappers>
          <!--加载sql映射文件，填写映射文件的路径-->
          <!--<mapper resource="com/itheima/mapper/UserMapper.xml"/>-->
          <!--  以包扫描的方式进行配置，直接获取该package中所有的文件。  -->
          <package name="com.itheima.mapper"/>
      </mappers>
  </configuration>
  ```

* sql映射文件，`[表名]Mapper.xml`

  * namespace：名称空间
  * resultType：返回类型，选择一个已有的类
  * id：此条sql语句的唯一标识

  ```xml
  <?xml version="1.0" encoding="UTF-8" ?>
  <!DOCTYPE mapper
          PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
          "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
  
  <!--在这里进行配置-->
  <mapper namespace="myNamespace">
      <select id="selectAll" resultType="com.itheima.pojo.User">
          select * from tb_user;
      </select>
  </mapper>
  ```

* 代码编写：

  * 通过SqlSession对象执行sql：
    * selectList查询多条，selectOne查询一条。
    * 以`namespace.id`的形式确定sql语句。

### troubleshooting

* 不支持的发行版本

  [错误“ Java：不支持发行版本5”的正确解决方案_星神学Java的博客-CSDN博客_不支持发行版本5](https://blog.csdn.net/qq_41428418/article/details/124169175)

### 配置IDEA与数据库连接

以获得更好的代码提示

* 在Database边栏中对数据库进行连接和配置。

## Mapper代理开发

* Mapper代理方式：允许以方法的形式，调用sql语句，避免硬编码。

### SQL映射文件 Mapper.xml

* Mapper代理开发方式的要求：

  1. 定义与SQL映射xml文件同名的Mapper interface，且该接口文件和SQL xml映射文件需放在同一目录下。

     * 保证java目录下，Mapper interface文件所处的package名称，与resources目录下，sql xml映射文件所处的package名称相同。

       ![](.\noteimages\mabatis mapper代理开发 目录结构.PNG)

  2. 设置SQL xml配置文件的namespace属性为Mapper接口的**全限定名**（全类名）。

  3. 在Mapper interface中定义方法，方法名同SQL映射文件中sql语句的id，且保持参数类型和返回类型一致。

  4. 使用Mapper代理：

     1. 通过sqlSession的getMapper方法，以反射的形式获取Mapper接口的代理对象。
     2. 调用与sql语句id对应的方法，执行sql语句。

     ```java
      // 3. 通过Mapper代理对象执行sql
     // 3.1 获取UserMapper接口的代理对象
     UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
     // 3.2 通过Mapper代理对象执行sql语句。
     List<User> users = userMapper.selectAll();
     ```

### 配置文件 mybatis-config.xml

#### environment

用于配置数据库连接环境信息。

* 可以配置多个environment，通过default属性进行切换。

#### typeAlias

可以用于给类型起别名，方便在对sql语句指定参数和返回类型是使用。

[mybatis – MyBatis 3 | Configuration](https://mybatis.org/mybatis-3/configuration.html#typeAliases)

* 默认别名为类名本身，不区分大小写，配置别名后可省略包名。

```xml
  <typeAliases>
        <package name="com.itheima.pojo"/>
    </typeAliases>
```

#### MyBatisX Plugin

IDEA插件，用于在sql映射xml文件和mapper接口之间快速跳转。

### 数据库到实体类字段的转换

* 需要将字段从数据库的snake_case到java的camelCase：

  * 在sql语句中起别名，让别名和实体类的字段名一样。

    * 可以使用sql片段`<sql id="xxx"> <include refid="xxx">`

  * 使用result map标签进行映射

    * `<resultMap>`标签的attribute：id唯一标识，type：映射的实体类名

      子标签：

      * id：用于映射主键
      * result：用于映射其他列。

    * 使用resultMap：在sql语句标签处，用resultMap attribute替代原resultType attribute。

    ```xml
    <mapper namespace="com.itheima.mapper.BrandMapper">
        <resultMap id="brandResultMap" type="com.itheima.pojo.Brand">
            <result column="brand_name" property="brandName"/>
            <result column="company_name" property="companyName"/>
        </resultMap>
    
        <select id="selectAll" resultMap="brandResultMap">
            select *
            from tb_brand;
        </select>
    </mapper>
    ```

## 条件查询

### 参数占位符

共有2种，`#{参数}`和`${参数}`：

* `#{参数}`：会将参数转换成`?`，可防止sql注入。

* `${参数}`：直接进行字符串拼接，用于在表名/列名不固定时，进行sql动态拼接的场景。会有sql注入问题。

* 可以通过parameterType指定参数类型，也可省略，因为在接口中指定了参数类型，无需重复指定。

* 多个参数的处理：

  * 散装参数：在接口中使用`@Params("SQL中参数占位符名称")`注解为每一个参数进行标注。

    ```java
    List<Brand> selectByCondition(@Param("status") int status, @Param("companyName") String companyName, @Param("brandName") String brandName);
    ```

  * 对象参数：直接传入一个对象，对象中的属性名与sql中变量名一致。

    ```java
    // mapper interface
    List<Brand> selectByCondition(Brand brand);
    
    // 构造参数对象
    Brand brand = new Brand();
    brand.setStatus(1);
    brand.setCompanyName(companyName);
    brand.setBrandName(brandName);
    
    // 执行sql语句
    List<Brand> brands = brandMapper.selectByCondition(brand);
    ```

  * Map集合参数，传入一个map（如HashMap），key、value分别为sql中的参数、参数值。

    ```java
    // mapper interface
    List<Brand> selectByCondition(Map map);
    
     // 构造map
    Map<String, Object> map = new HashMap<>();
    map.put("status", status);
    map.put("companyName", companyName);
    map.put("brandName", brandName);
    
    List<Brand> brands = brandMapper.selectByCondition(map);
    ```

### 特殊字符处理

在xml中写sql语句，`<`会解释为标签开头，这些特殊字符需要进行处理，有2种方式：

* 转义字符，`<`->`&It`
* 将特殊字符放入CDATA区。

### 动态SQL

[mybatis – MyBatis 3 | Dynamic SQL](https://mybatis.org/mybatis-3/dynamic-sql.html)

* 通过一些MyBatis提供的逻辑标签，来实现动态SQL：

  * if else：
    * 通过在if标签的test attribute中加入逻辑语句，来进行判断。
  * choose when

* 动态SQL的标签中的逻辑判断语句，类似于SQL的写法：

  * 逻辑运算符使用`and,or`等
  * 对字符串的判断直接使用`=`


#### 多条件查询 if where

* 比如，用户使用 状态+公司名+品牌名 3个条件进行查询时。用户可能只是用其中1-2个条件，此时需要使用if/where标签来动态拼接sql语句。

  ```xml
  <select id="selectByCondition" resultMap="brandResultMap">
          select *
          from tb_brand
          where
          <if test="status != null">
              status = ${status}
          </if>
          <if test="companyName != null and companyName != ''">
              and company_name like #{companyName}
          </if>
          <if test="brandName != null and companyName != ''">
              and brand_name like #{brandName};
          </if>
  </select>
  ```

* 对于第一个条件的特殊处理：

  * 上述sql语句，如果第一个条件status为null，会导致sql语句where 直接拼接and，出现错误。

  * 解决方案：

    * 给第一个条件也加上and，在where后加上`1=1`恒等式，所有if内的sql语句均用and拼接。

      ```xml
      <select id="selectByCondition" resultMap="brandResultMap">
              select *
              from tb_brand
              where 1=1
              <if test="status != null">
                  and status = ${status}
              </if>
              <if test="companyName != null and companyName != ''">
                  and company_name like #{companyName}
              </if>
              <if test="brandName != null and companyName != ''">
                  and brand_name like #{brandName};
              </if>
      </select>
      ```

    * 使用where标签替代`where`关键字，my batis会自动补充and和去除`and`关键字

      where标签会根据子句是否存在自动添加where关键字，并根据语法是否正确，自动去除子句句首的and或or关键字。
      
      ```xml
         <select id="selectByCondition" resultMap="brandResultMap">
              select *
              from tb_brand
             
              <where>
                  <if test="status != null">
                      status = ${status}
                  </if>
                  <if test="companyName != null and companyName != ''">
                      and company_name like #{companyName}
                  </if>
                  <if test="brandName != null and companyName != ''">
                      and brand_name like #{brandName};
                  </if>
              </where>
      
          </select>
      ```
      

#### 单条件查询 choose-when-otherwise

* 比如，让用户选择，通过 状态/公司名/品牌名 三个条件之一进行查询。

* choose-when-otherwise的使用类似java中的switch-case-default。

* 对于用户没有选择条件的情况，可以使用otherwise或where进行处理。

  * otherwise:

    ```xml
    <select id="selectBySingleCondition" resultType="com.itheima.pojo.Brand">
        select * from tb_brand where
        <choose>
        <when test="status != null">
        status = ${status}
    </when>
        <when test="companyName != null and companyName != ''">
        company_name like #{companyName}
    </when>
        <when test="brandName != null and companyName != ''">
        brand_name like #{brandName};
    </when>
        <otherwise>
        1=1
        </otherwise>
        </choose>
        </select>
    ```

    

  * where: 

    ```xml
    <select id="selectBySingleCondition" resultType="com.itheima.pojo.Brand">
        select * from tb_brand
        <where>
            <choose>
                <when test="status != null">
                    status = ${status}
                </when>
                <when test="companyName != null and companyName != ''">
                    company_name like #{companyName}
                </when>
                <when test="brandName != null and companyName != ''">
                    brand_name like #{brandName};
                </when>
            </choose>
        </where>
    </select>
    ```


## 插入语句

* mybatis中插入语句事务默认需要手动提交，openSession方法默认用于开启事务。

  ```shell
  [DEBUG] 22:53:38.707 [main] o.a.i.t.j.JdbcTransaction - Setting autocommit to false on JDBC Connection [com.mysql.jdbc.JDBC4Connection@2438dcd] 
  ```

* 通过sqlSession的commit方法提交事务。

  ```java
  sqlSession.commit();
  ```

* 也可以在创建sqlSession对象时设置autoCommit为true，自动提交事务。

  ```java
  SqlSession sqlSession = sqlSessionFactory.openSession(true);
  ```

### 插入后返回主键

* 在sql映射文件中，insert语句中，添加`useGeneratedKeys="true"`和`keyProperty="主键名称"`两个attribute。

  ```xml
   <insert id="add" useGeneratedKeys="true" keyProperty="id">
          insert into tb_brand (brand_name, company_name, ordered, description, status)
          values (#{brandName}, #{companyName}, #{ordered}, #{description}, #{status});
  </insert>
  ```

* 插入后，通过参数对象的getId方法，获得主键。

  ```java
  // 以对象形式传入参数，参数名与sql中变量对应
  brandMapper.add(brand);
  // 获取主键
  System.out.println("id为：" + brand.getId());
  ```

## 修改 set

* 对所有字段进行修改

* 通过set标签，对用户选择的字段，利用动态sql进行修改

  ```xml
  <update id="update">
          update tb_brand
          <set>
              <if test="companyName != null and companyName != ''">
                  company_name=#{companyName},
              </if>
              <if test="brandName != null and companyName != ''">
                  brand_name=#{brandName},
              </if>
              <if test="ordered != null">
                  ordered=#{ordered},
              </if>
              <if test="description != null and description != ''">
                  description=#{description},
              </if>
              <if test="status != null">
                  status=#{status},
              </if>
          </set>
          where id = #{id};
      </update>
  ```

## 删除 foreach

* 删除一个

* 批量删除

  `delete from tb_brand where id in (?,?,?...);`

  * 使用foreach标签，来遍历数组，从而在sql语句的in关键字中生成多个参数。

  * mybatis会将数组类型的参数，封装成一个Map集合，默认的key为array。如果需要改名，要通过`@Param`注解指定顶名称。

  * `<foreach>`的attribute：

    * collection：数组参数名称，默认为`array`，可以通过`@Param`注解改名。
    * separator：来指定分隔符，如in可以指定为`,`。
    * item：遍历得到数组项的名称
    * open, close：在遍历的开始和结束拼接的内容，比如可以用来加上括号。

    ```java
    // mapper interface
    void deleteByIds(@Param("ids") int[] ids);
    ```

    ```xml
    <delete id="deleteByIds">
            delete from tb_brand where id in
            <foreach collection="ids" item="id" separator="," open="(" close=")">
                #{id}
            </foreach>
        </delete>
    ```

## 参数传递 源码分析

* MyBatis会将多个参数封装为Map集合，sql语句中通过Map中的key获取相应参数值。

* 在不设置`@Param`注解的情况下，默认的key为`arg0,arg1....`或`param1,param2...`两种形式。

* MyBatis通过`ParamNameResolver`类来处理参数传递。

  通过其`getNamedParams`方法对参数进行处理。

* 对于集合类型Collection、List、Array的处理：
  * 将其put到Map中，除了arg0作为key之外：
    * Collection会将collection作为key
    * List会将collection和list作为key
    * Array会将array作为key
* 建议都不要用默认的key名称，而是使用`@Param`注解。

```java
public static Object wrapToMapIfCollection(Object object, String actualParamName) {
        ParamMap map;
        if (object instanceof Collection) {
            map = new ParamMap();
            map.put("collection", object);
            if (object instanceof List) {
                map.put("list", object);
            }

            Optional.ofNullable(actualParamName).ifPresent((name) -> {
                map.put(name, object);
            });
            return map;
        } else if (object != null && object.getClass().isArray()) {
            map = new ParamMap();
            map.put("array", object);
            Optional.ofNullable(actualParamName).ifPresent((name) -> {
                map.put(name, object);
            });
            return map;
        } else {
            return object;
        }
    }
```

## 注解开发

* 最好只在sql语句简单的情况下使用注解

  >The annotations are a lot cleaner for simple statements, however, Java Annotations are both limited and messier for more complicated statements. Therefore, if you have to do anything complicated, you're better off with XML mapped statements.

* 常用的增删改查的注解：
  * `@Select`
  * `@Insert`
  * `@Update`
  * `@Delete`

```java
@Select("select * from tb_brand where id = #{id}")
Brand selectById(int id);
```

## mybatis常用Snippet

均从官网复制

### mybatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "https://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
  <environments default="development">
    <environment id="development">
      <transactionManager type="JDBC"/>
      <dataSource type="POOLED">
        <property name="driver" value="${driver}"/>
        <property name="url" value="${url}"/>
        <property name="username" value="${username}"/>
        <property name="password" value="${password}"/>
      </dataSource>
    </environment>
  </environments>
  <mappers>
    <mapper resource="org/mybatis/example/BlogMapper.xml"/>
  </mappers>
</configuration>
```

### Mapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.mybatis.example.BlogMapper">
  <select id="selectBlog" resultType="Blog">
    select * from Blog where id = #{id}
  </select>
</mapper>
```

### 获取SqlSessionFactory对象

```java
String resource = "org/mybatis/example/mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory =
  new SqlSessionFactoryBuilder().build(inputStream);
```

