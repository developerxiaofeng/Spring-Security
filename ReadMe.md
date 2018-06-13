![Image text](https://github.com/developerxiaofeng/Spring-Security/blob/master/src/main/resources/static/666.png)
如图通用的用户权限模型,一般情况会有5张表,分别是用户表,角色表,权限表,用户角色关系表,角色权限对应表
一般,资源的分配是基于角色分配的(即,资源访问权限赋予非角色,用户通过角色进而拥有权限);而访问资源的时候是基于资源权限去授权判断的
Spring Security和Apache Shiro是两个应用比较多的权限管理框架.Spring Security依赖Spring,其功能强大,相对于Shiro学习难度较大
养成一种习惯,再是吐出的东西,也要自己敲代码走一遍,不然你会忘得一干二净

    Spring Security致力于Java提供认证和授权管理,它是一个强大,高度自定义的认证和访问控制框架
两个关键词Authentication(认证)和Authorization(授权)
    认证是验证用户身份的合法性,而授权是控制你可以做什么
接下啦了解几个接口
AuthenticationProvider
此接口是用于认证,通过实现这个接口来定制我们自己认证逻辑,默认的实现类是JaasAuthentication,它的全称是Java Authentication and Authentication Service(JAAS)

AccessDecisionManager
此接口用于访问控制,他决定是否可以访问某个资源,实现这个接口可以定制我们授权的逻辑
AccessDecisionVoter 投票器
在授权的时候通过投票的方式来决定用户是否可以访问,这里涉及投票规则
UserDetailsService
是用于加载特定用户信息,他只有一个接口通过指定用户名去查询用户
UserDetails
代表用户信息,即主体相当于Shiro中Subject,User是他的一个实现