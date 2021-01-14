> mybatis-plus简称MP是一个 Mybatis 的增强工具，在 Mybatis 的基础上只做增强不做改变，为简化开发、提高效率而生。

这是官方给的定义，关于mybatis-plus的更多介绍及特性，可以参考mybatis-plus[官网](https://mybatis.plus/) 。那么它是怎么增强的呢？其实就是它已经封装好了一些crud方法，我们不需要再写xml了，直接调用这些方法就行，就类似于JPA。

下面是在`MP`为基础封装了一个查询类，实现了如果需要增加查询条件只需在前端修改即可。             
# 查询工具
* SearchModel
```java
public class SearchModel<T> {

    private Integer pageIndex;
    private Integer pageSize;
    private List<Field> fields;
    private String orderField;
    private boolean isAsc;

    public IPage<T> getPage() {
        IPage<T> page = new Page<>(pageIndex, pageSize);
        if (!StringUtil.isEmptyOrNull(orderField)) {
            OrderItem orderItem = new OrderItem();
            orderItem.setAsc(isAsc);
            orderItem.setColumn(orderField);
            page.orders().add(orderItem);
        }
        return page;

    }

    public QueryWrapper<T> getQueryModel() {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        for (Iterator iter = this.fields.iterator(); iter.hasNext(); ) {
            Field field = (Field) iter.next();
            switch (field.getQueryMethod()) {
                case eq:
                    queryWrapper.eq(true, field.getName(), field.getValue());
                    break;
                case like:
                    queryWrapper.like(true, field.getName(), field.getValue());
            }
        }

        if (!StringUtil.isEmptyOrNull(orderField)) {
            queryWrapper.orderBy(true, isAsc, orderField);
        }
        return queryWrapper;
    }
}
```                
* Field
```java
public class Field {
    public Field(String name, Object value) {
        this.name = name;
        this.value = value;
        this.queryMethod = QueryMethod.eq;
    }

    public Field(String name, Object value, QueryMethod queryMethod) {
        this.name = name;
        this.value = value;
        this.queryMethod = queryMethod;
    }

    private String name;
    private Object value;
    private QueryMethod queryMethod;
}
```    
* QueryMethod
```java
public enum QueryMethod {
    eq, like
}

```
## 调用示例

```json
 {
 "fields": [
             {
             "value": "v",
             "name": "project_code",
             "queryMethod": "eq"
             },
             {
             "name": "type",
             "queryMethod": "like",
            "value": "b"
             },
             {
             "name": "id",
             "queryMethod": "like",
             "value": "a"
             }
 ],
 "pageIndex": 1,
 "pageSize": 8,
 "orderField": "type",
 "isAsc": "false"
 }
```
在api中传入上面的json对象即可完成一个查询服务,查询条件通过前端传入的字段控制
# BaseService
* IBaseService
```java

public interface IBaseService<T> {

    T save(T entity) throws Exception;

    boolean saveBatch(Collection<T> entityList);


    // TableId 注解存在更新记录，否插入一条记录
    boolean saveOrUpdate(T entity);

    // 根据updateWrapper尝试更新，否继续执行saveOrUpdate(T)方法
    boolean saveOrUpdate(T entity, Wrapper<T> updateWrapper);

    // 批量修改插入
    boolean saveOrUpdateBatch(Collection<T> entityList);

    // 根据 entity 条件，删除记录
    boolean remove(Wrapper<T> queryWrapper);

    // 根据 ID 删除
    boolean removeById(Serializable id);

    // 根据 columnMap 条件，删除记录
    boolean removeByMap(Map<String, Object> columnMap);

    // 删除（根据ID 批量删除）
    boolean removeByIds(Collection<? extends Serializable> idList);

    List<T> list();

    // 查询列表
    List<T> list(SearchModel<T> searchModel);

    // 查询（根据ID 批量查询）
    Collection<T> listByIds(Collection<? extends Serializable> idList);

    // 查询（根据 columnMap 条件）
    Collection<T> listByMap(Map<String, Object> columnMap);

    // 翻页查询
    IPage<T> page(SearchModel<T> searchModel);

    T selectById(Serializable id);

    T selectOne(Wrapper<T> queryWrapper);
}

```

* BaseServiceImpl
 ```java
public class BaseServiceImpl<M extends BaseMapper<T>, T> implements IBaseService<T> {

    @Autowired
    private M baseMapper;


    @Override
    public T save(T entity) throws Exception {
        baseMapper.insert(entity);
        return entity;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public boolean saveBatch(Collection<T> entityList) {
        Integer size = entityList.size();
        for (T entity : entityList) {
            baseMapper.insert(entity);
            size++;
        }
        return size == entityList.size();
    }

    @Override
    public boolean saveOrUpdate(T entity) {
        int rs = baseMapper.updateById(entity);
        if (rs > 0) return true;
        return baseMapper.insert(entity) > 0;
    }

    @Override
    public boolean saveOrUpdate(T entity, Wrapper<T> updateWrapper) {


        return false;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public boolean saveOrUpdateBatch(Collection<T> entityList) {

        for (T entity : entityList) {
            saveOrUpdate(entity);
        }
        return true;
    }

    @Override
    public boolean remove(Wrapper<T> queryWrapper) {
        return baseMapper.delete(queryWrapper) > 0;
    }

    @Override
    public boolean removeById(Serializable id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public boolean removeByMap(Map<String, Object> columnMap) {
        return baseMapper.deleteByMap(columnMap) > 0;
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        return baseMapper.deleteBatchIds(idList) > 0;
    }

    @Override
    public List<T> list() {
        return baseMapper.selectList(new QueryWrapper<T>());
    }

    @Override
    public List<T> list(SearchModel<T> searchModel) {
        return baseMapper.selectList(searchModel.getQueryModel());
    }

    @Override
    public Collection<T> listByIds(Collection<? extends Serializable> idList) {
        return baseMapper.selectBatchIds(idList);
    }

    @Override
    public Collection<T> listByMap(Map<String, Object> columnMap) {
        return baseMapper.selectByMap(columnMap);
    }


    @Override
    public IPage<T> page(SearchModel<T> searchModel) {
        return baseMapper.selectPage(searchModel.getPage(), searchModel.getQueryModel());

    }

    @Override
    public T selectById(Serializable id) {
        return baseMapper.selectById(id);
    }

    @Override
    public T selectOne(Wrapper<T> queryWrapper) {
        return baseMapper.selectOne(queryWrapper);
    }
}

```

MP实现了mapper层基础的CRUD方法，这里把一些常用的service层的方法整理了一下，又减少了一些代码量
 * Maven
```
<!-- https://mvnrepository.com/artifact/com.github.codeinghelper/mp-plus -->
<dependency>
    <groupId>com.github.codeinghelper</groupId>
    <artifactId>mp-plus</artifactId>
    <version>0.0.1</version>
</dependency>

```
* Gradle
```
// https://mvnrepository.com/artifact/com.github.codeinghelper/mp-plus
compile group: 'com.github.codeinghelper', name: 'mp-plus', version: '0.0.1'

```
