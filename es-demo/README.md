# Setup Elasticsearch Locally
This document describe the steps to run a standalone Elasticsearch
locally using docker image, and how to re-produce the `http 429 error`.

### run the official docker image with default settings
```shell
docker run --name es01 --net elastic -p 9200:9200 -e "discovery.type=single-node" -it docker.elastic.co/elasticsearch/elasticsearch:8.6.2
```

---
### mount the docker's volume and copy the config folder to local
```shell
docker cp es01:/usr/share/elasticsearch/config ./config/
```

---
### adjust the thread_pool for both read and write
edit the elasticsearch.yml inside the config folder and append below settings
```yaml
thread_pool:
  search:
    size: 1
    queue_size: 5
  write:
    size: 1
    queue_size: 5
```

---
### re-run the official docker image with updated settings
re-run the official docker image and mount the local config folder as elasticsearch config folder
```shell
docker run --name es01 \
--net elastic -p 9200:9200 \
-e "discovery.type=single-node" \
-v /Users/kwg06/config:/usr/share/elasticsearch/config \
-it docker.elastic.co/elasticsearch/elasticsearch:8.6.2
```

---
### retrieve the generated password
once elasticsearch is started, it will output the password,
copy it for subsequent authentication with elasticsearch's API
```shell
✅ Elasticsearch security features have been automatically configured!
✅ Authentication is enabled and cluster connections are encrypted.

Password for the elastic user (reset with `bin/elasticsearch-reset-password -u elastic`):
  pL8HZ+N-EclvFL=vEV8Y  
```

---
### verify the thread_pool settings
```shell
curl -X GET https://localhost:9200/_nodes/thread_pool --cacert config/certs/http_ca.crt -u elastic:VhI2sNAmr+0PSAXF+hzz
```

### run parallel curl requests
```shell
#!/bin/bash
clear

seq 1 200 | xargs -I $ -n1 -P200 \
curl -i -X POST https://localhost:9200/customer/_doc/1  -H 'Content-Type: application/json' -d'
{
"firstname": "Jennifer",
"lastname": "Walters"
}
' --cacert config/certs/http_ca.crt -u elastic:VhI2sNAmr+0PSAXF+hzz  
```

---
### Run parallel read/write requests using `Spring Data Elastic Search` library
```java
@SpringBootTest
public class DemoRepositoryIT {

    @Autowired
    DemoRepository demoRepository;

    @Autowired
    ElasticsearchOperations elasticsearchOperations;

    @Test
    public void test() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Callable<Void>> callables = new LinkedList<>();
        for (int i = 0; i < 50; i++) {
            callables.add(() -> {
                try {
                    DemoEvent demoEvent = new DemoEvent();
                    demoEvent.setDetail("hello");
                    demoRepository.save(demoEvent);
                    Optional<DemoEvent> savedDemoEvent = demoRepository.findById(demoEvent.getid());
                    assertTrue(savedDemoEvent.isPresent());
                    demoRepository.deleteById(savedDemoEvent.get().getid());
                    return null;
                } catch (RuntimeException e) {
                    throw e;
                }
            });
        }

        Collection<Future<Void>> futures = executorService.invokeAll(callables);
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            Thread.sleep(1000);
        }
        executorService.shutdownNow();
    }
}
```

---
### http error
```shell
HTTP/1.1 429 Too Many Requests
X-elastic-product: Elasticsearch
content-type: application/json;charset=utf-8
content-length: 1965
{
  "error": {
  "root_cause": [
    {
      "type": "es_rejected_execution_exception",
      "reason": "rejected execution of org.elasticsearch.action.bulk.TransportBulkAction$1/org.elasticsearch.action.ActionListener$RunBeforeActionListener/org.elasticsearch.action.ActionListener$DelegatingFailureActionListener/org.elasticsearch.action.support.ContextPreservingActionListener/WrappedActionListener{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7179/0x00000008020b8000@55193115}{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7180/0x00000008020b8210@68a2670e}/org.elasticsearch.xpack.security.action.filter.SecurityActionFilter$$Lambda$6132/0x0000000801f57198@39035b42/org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7185/0x00000008020b8cf8@4d3028e4 on EsThreadPoolExecutor[name = 0a2eceac1ca2/write, queue capacity = 5, org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor@7bc6b117[Running, pool size = 1, active threads = 1, queued tasks = 5, completed tasks = 3688]]"
    }
  ],
  "type": "es_rejected_execution_exception",
  "reason": "rejected execution of org.elasticsearch.action.bulk.TransportBulkAction$1/org.elasticsearch.action.ActionListener$RunBeforeActionListener/org.elasticsearch.action.ActionListener$DelegatingFailureActionListener/org.elasticsearch.action.support.ContextPreservingActionListener/WrappedActionListener{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7179/0x00000008020b8000@55193115}{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7180/0x00000008020b8210@68a2670e}/org.elasticsearch.xpack.security.action.filter.SecurityActionFilter$$Lambda$6132/0x0000000801f57198@39035b42/org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7185/0x00000008020b8cf8@4d3028e4 on EsThreadPoolExecutor[name = 0a2eceac1ca2/write, queue capacity = 5, org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor@7bc6b117[Running, pool size = 1, active threads = 1, queued tasks = 5, completed tasks = 3688]]"
  },
  "status": 429
}
```

---
### java exception trace
```shell
java.util.concurrent.ExecutionException: org.springframework.dao.DataAccessResourceFailureException: method [DELETE], host [https://localhost:9200], URI [/log/_doc/LDbf8oYBW_RVQjX8J1e4?refresh=false], status line [HTTP/1.1 429 Too Many Requests]
{"error":{"root_cause":[{"type":"es_rejected_execution_exception","reason":"rejected execution of org.elasticsearch.action.bulk.TransportBulkAction$1/org.elasticsearch.action.ActionListener$RunBeforeActionListener/org.elasticsearch.action.ActionListener$DelegatingFailureActionListener/org.elasticsearch.action.support.ContextPreservingActionListener/WrappedActionListener{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7179/0x00000008020b8000@85f7ca9}{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7180/0x00000008020b8210@6e7dc45}/org.elasticsearch.xpack.security.action.filter.SecurityActionFilter$$Lambda$6132/0x0000000801f57198@468e39d8/org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7185/0x00000008020b8cf8@5bf49a9f on EsThreadPoolExecutor[name = 0a2eceac1ca2/write, queue capacity = 5, org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor@7bc6b117[Running, pool size = 1, active threads = 1, queued tasks = 5, completed tasks = 3749]]"}],"type":"es_rejected_execution_exception","reason":"rejected execution of org.elasticsearch.action.bulk.TransportBulkAction$1/org.elasticsearch.action.ActionListener$RunBeforeActionListener/org.elasticsearch.action.ActionListener$DelegatingFailureActionListener/org.elasticsearch.action.support.ContextPreservingActionListener/WrappedActionListener{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7179/0x00000008020b8000@85f7ca9}{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7180/0x00000008020b8210@6e7dc45}/org.elasticsearch.xpack.security.action.filter.SecurityActionFilter$$Lambda$6132/0x0000000801f57198@468e39d8/org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7185/0x00000008020b8cf8@5bf49a9f on EsThreadPoolExecutor[name = 0a2eceac1ca2/write, queue capacity = 5, org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor@7bc6b117[Running, pool size = 1, active threads = 1, queued tasks = 5, completed tasks = 3749]]"},"status":429}

	at java.base/java.util.concurrent.FutureTask.report(FutureTask.java:122)
	at java.base/java.util.concurrent.FutureTask.get(FutureTask.java:191)
	at com.example.demo.DemoRepositoryIT.test(DemoRepositoryTests.java:69)
	at com.example.demo.DemoRepositoryIT.test_001(DemoRepositoryTests.java:30)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:568)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:727)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$7(TestMethodTestDescriptor.java:217)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35)
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57)
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:147)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:127)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:90)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:55)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:102)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:54)
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:114)
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:86)
	at org.junit.platform.launcher.core.DefaultLauncherSession$DelegatingLauncher.execute(DefaultLauncherSession.java:86)
	at org.junit.platform.launcher.core.SessionPerRequestLauncher.execute(SessionPerRequestLauncher.java:53)
	at com.intellij.junit5.JUnit5IdeaTestRunner.startRunnerWithArgs(JUnit5IdeaTestRunner.java:57)
	at com.intellij.rt.junit.IdeaTestRunner$Repeater$1.execute(IdeaTestRunner.java:38)
	at com.intellij.rt.execution.junit.TestsRepeater.repeat(TestsRepeater.java:11)
	at com.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:35)
	at com.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:235)
	at com.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:54)
Caused by: org.springframework.dao.DataAccessResourceFailureException: method [DELETE], host [https://localhost:9200], URI [/log/_doc/LDbf8oYBW_RVQjX8J1e4?refresh=false], status line [HTTP/1.1 429 Too Many Requests]
{"error":{"root_cause":[{"type":"es_rejected_execution_exception","reason":"rejected execution of org.elasticsearch.action.bulk.TransportBulkAction$1/org.elasticsearch.action.ActionListener$RunBeforeActionListener/org.elasticsearch.action.ActionListener$DelegatingFailureActionListener/org.elasticsearch.action.support.ContextPreservingActionListener/WrappedActionListener{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7179/0x00000008020b8000@85f7ca9}{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7180/0x00000008020b8210@6e7dc45}/org.elasticsearch.xpack.security.action.filter.SecurityActionFilter$$Lambda$6132/0x0000000801f57198@468e39d8/org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7185/0x00000008020b8cf8@5bf49a9f on EsThreadPoolExecutor[name = 0a2eceac1ca2/write, queue capacity = 5, org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor@7bc6b117[Running, pool size = 1, active threads = 1, queued tasks = 5, completed tasks = 3749]]"}],"type":"es_rejected_execution_exception","reason":"rejected execution of org.elasticsearch.action.bulk.TransportBulkAction$1/org.elasticsearch.action.ActionListener$RunBeforeActionListener/org.elasticsearch.action.ActionListener$DelegatingFailureActionListener/org.elasticsearch.action.support.ContextPreservingActionListener/WrappedActionListener{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7179/0x00000008020b8000@85f7ca9}{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7180/0x00000008020b8210@6e7dc45}/org.elasticsearch.xpack.security.action.filter.SecurityActionFilter$$Lambda$6132/0x0000000801f57198@468e39d8/org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7185/0x00000008020b8cf8@5bf49a9f on EsThreadPoolExecutor[name = 0a2eceac1ca2/write, queue capacity = 5, org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor@7bc6b117[Running, pool size = 1, active threads = 1, queued tasks = 5, completed tasks = 3749]]"},"status":429}
at org.springframework.data.elasticsearch.client.elc.ElasticsearchExceptionTranslator.translateExceptionIfPossible(ElasticsearchExceptionTranslator.java:102)
at org.springframework.data.elasticsearch.client.elc.ElasticsearchExceptionTranslator.translateException(ElasticsearchExceptionTranslator.java:62)
at org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate.execute(ElasticsearchTemplate.java:540)
at org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate.doDelete(ElasticsearchTemplate.java:244)
at org.springframework.data.elasticsearch.core.AbstractElasticsearchTemplate.delete(AbstractElasticsearchTemplate.java:317)
at org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository.lambda$doDelete$10(SimpleElasticsearchRepository.java:277)
at org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository.executeAndRefresh(SimpleElasticsearchRepository.java:344)
at org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository.doDelete(SimpleElasticsearchRepository.java:277)
at org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository.deleteById(SimpleElasticsearchRepository.java:226)
at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
at java.base/java.lang.reflect.Method.invoke(Method.java:568)
at org.springframework.data.repository.core.support.RepositoryMethodInvoker$RepositoryFragmentMethodInvoker.lambda$new$0(RepositoryMethodInvoker.java:288)
at org.springframework.data.repository.core.support.RepositoryMethodInvoker.doInvoke(RepositoryMethodInvoker.java:136)
at org.springframework.data.repository.core.support.RepositoryMethodInvoker.invoke(RepositoryMethodInvoker.java:120)
at org.springframework.data.repository.core.support.RepositoryComposition$RepositoryFragments.invoke(RepositoryComposition.java:516)
at org.springframework.data.repository.core.support.RepositoryComposition.invoke(RepositoryComposition.java:285)
at org.springframework.data.repository.core.support.RepositoryFactorySupport$ImplementationMethodExecutionInterceptor.invoke(RepositoryFactorySupport.java:628)
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.doInvoke(QueryExecutorMethodInterceptor.java:168)
at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.invoke(QueryExecutorMethodInterceptor.java:143)
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97)
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:218)
at jdk.proxy2/jdk.proxy2.$Proxy79.deleteById(Unknown Source)
at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
at java.base/java.lang.reflect.Method.invoke(Method.java:568)
at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:343)
at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196)
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:137)
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:218)
at jdk.proxy2/jdk.proxy2.$Proxy79.deleteById(Unknown Source)
at com.example.demo.DemoRepositoryIT.lambda$test$0(DemoRepositoryTests.java:62)
at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
at java.base/java.lang.Thread.run(Thread.java:833)
Caused by: java.lang.RuntimeException: method [DELETE], host [https://localhost:9200], URI [/log/_doc/LDbf8oYBW_RVQjX8J1e4?refresh=false], status line [HTTP/1.1 429 Too Many Requests]
{"error":{"root_cause":[{"type":"es_rejected_execution_exception","reason":"rejected execution of org.elasticsearch.action.bulk.TransportBulkAction$1/org.elasticsearch.action.ActionListener$RunBeforeActionListener/org.elasticsearch.action.ActionListener$DelegatingFailureActionListener/org.elasticsearch.action.support.ContextPreservingActionListener/WrappedActionListener{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7179/0x00000008020b8000@85f7ca9}{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7180/0x00000008020b8210@6e7dc45}/org.elasticsearch.xpack.security.action.filter.SecurityActionFilter$$Lambda$6132/0x0000000801f57198@468e39d8/org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7185/0x00000008020b8cf8@5bf49a9f on EsThreadPoolExecutor[name = 0a2eceac1ca2/write, queue capacity = 5, org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor@7bc6b117[Running, pool size = 1, active threads = 1, queued tasks = 5, completed tasks = 3749]]"}],"type":"es_rejected_execution_exception","reason":"rejected execution of org.elasticsearch.action.bulk.TransportBulkAction$1/org.elasticsearch.action.ActionListener$RunBeforeActionListener/org.elasticsearch.action.ActionListener$DelegatingFailureActionListener/org.elasticsearch.action.support.ContextPreservingActionListener/WrappedActionListener{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7179/0x00000008020b8000@85f7ca9}{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7180/0x00000008020b8210@6e7dc45}/org.elasticsearch.xpack.security.action.filter.SecurityActionFilter$$Lambda$6132/0x0000000801f57198@468e39d8/org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7185/0x00000008020b8cf8@5bf49a9f on EsThreadPoolExecutor[name = 0a2eceac1ca2/write, queue capacity = 5, org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor@7bc6b117[Running, pool size = 1, active threads = 1, queued tasks = 5, completed tasks = 3749]]"},"status":429}
at org.springframework.data.elasticsearch.client.elc.ElasticsearchExceptionTranslator.translateException(ElasticsearchExceptionTranslator.java:61)
... 41 more
Caused by: org.elasticsearch.client.ResponseException: method [DELETE], host [https://localhost:9200], URI [/log/_doc/LDbf8oYBW_RVQjX8J1e4?refresh=false], status line [HTTP/1.1 429 Too Many Requests]
{"error":{"root_cause":[{"type":"es_rejected_execution_exception","reason":"rejected execution of org.elasticsearch.action.bulk.TransportBulkAction$1/org.elasticsearch.action.ActionListener$RunBeforeActionListener/org.elasticsearch.action.ActionListener$DelegatingFailureActionListener/org.elasticsearch.action.support.ContextPreservingActionListener/WrappedActionListener{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7179/0x00000008020b8000@85f7ca9}{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7180/0x00000008020b8210@6e7dc45}/org.elasticsearch.xpack.security.action.filter.SecurityActionFilter$$Lambda$6132/0x0000000801f57198@468e39d8/org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7185/0x00000008020b8cf8@5bf49a9f on EsThreadPoolExecutor[name = 0a2eceac1ca2/write, queue capacity = 5, org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor@7bc6b117[Running, pool size = 1, active threads = 1, queued tasks = 5, completed tasks = 3749]]"}],"type":"es_rejected_execution_exception","reason":"rejected execution of org.elasticsearch.action.bulk.TransportBulkAction$1/org.elasticsearch.action.ActionListener$RunBeforeActionListener/org.elasticsearch.action.ActionListener$DelegatingFailureActionListener/org.elasticsearch.action.support.ContextPreservingActionListener/WrappedActionListener{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7179/0x00000008020b8000@85f7ca9}{org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7180/0x00000008020b8210@6e7dc45}/org.elasticsearch.xpack.security.action.filter.SecurityActionFilter$$Lambda$6132/0x0000000801f57198@468e39d8/org.elasticsearch.action.bulk.TransportBulkAction$$Lambda$7185/0x00000008020b8cf8@5bf49a9f on EsThreadPoolExecutor[name = 0a2eceac1ca2/write, queue capacity = 5, org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor@7bc6b117[Running, pool size = 1, active threads = 1, queued tasks = 5, completed tasks = 3749]]"},"status":429}
at org.elasticsearch.client.RestClient.convertResponse(RestClient.java:347)
at org.elasticsearch.client.RestClient.performRequest(RestClient.java:313)
at org.elasticsearch.client.RestClient.performRequest(RestClient.java:288)
at co.elastic.clients.transport.rest_client.RestClientTransport.performRequest(RestClientTransport.java:147)
at co.elastic.clients.elasticsearch.ElasticsearchClient.delete(ElasticsearchClient.java:536)
at org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate.lambda$doDelete$8(ElasticsearchTemplate.java:244)
at org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate.execute(ElasticsearchTemplate.java:538)
... 40 more
```