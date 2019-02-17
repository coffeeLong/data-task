# data-task
数据任务处理架构设计，支持大数据任务管理，如大数据报表导出，线上测试导出220万数据只需13分钟

# data-task-base

导出报表基础架构设计，因为代码较简洁，部分接口和抽象类从语义命名

```java
核心类说明
DataTaskBuilder			提供了静态方法供用户创建DataTaskConfig、DataTaskManager对象和执行任务
DataTaskManager<T>		数据任务执行流程的实现类
DataTaskConfig<T>		配置数据任务的参数
DataService				定义获取数据的实现方法、参数等
```




# data-task-jdk

导出报表任务jdk实现，提供了threadpool和forkjoin两种线程池实现方式

# excel-import-export

excel导入导出实现，单元测试做了完整测试。如需集成spring boot可参考ExportTest.init()方法创建ExportTask对象