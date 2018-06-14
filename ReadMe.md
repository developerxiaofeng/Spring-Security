![Image text](https://github.com/developerxiaofeng/Spring-Security/blob/master/src/main/resources/static/666.png)

   如图通用的用户权限模型,一般情况会有5张表,分别是用户表,角色表,权限表,用户角色关系表,角色权限对应表
一般,资源的分配是基于角色分配的(即,资源访问权限赋予非角色,用户通过角色进而拥有权限);而访问资源的时候
是基于资源权限去授权判断的.Spring Security和Apache Shiro是两个应用比较多的权限管理框架.Spring 
Security依赖Spring,其功能强大,相对于Shiro学习难度较大'养成一种习惯,再是吐出的东西,也要自己敲代码走
一遍,不然你会忘得一干二净

    Spring Security致力于Java提供认证和授权管理,它是一个强大,高度自定义的认证和访问控制框架
一  两个关键词Authentication(认证)和Authorization(授权)
    认证是验证用户身份的合法性,而授权是控制你可以做什么
1 接下来了解几个接口
        AuthenticationProvider
        此接口是用于认证,通过实现这个接口来定制我们自己认证逻辑,默认的实现类是JaasAuthentication,
    它的全称是Java Authentication and Authentication Service(JAAS)           
        AccessDecisionManager
        此接口用于访问控制,他决定是否可以访问某个资源,实现这个接口可以定制我们授权的逻辑
        AccessDecisionVoter 投票器
        在授权的时候通过投票的方式来决定用户是否可以访问,这里涉及投票规则
        UserDetailsService
        是用于加载特定用户信息,他只有一个接口通过指定用户名去查询用户
        UserDetails
        代表用户信息,即主体相当于Shiro中Subject,User是他的一个实现
2 核心组件
2.1  SecurityContextHolder  用于存储安全上下文(securitycontext)的信息,当前用户是谁,该用户是否
    被认证,拥有哪些角色,默认使用ThreadLocal策略存储认证信息.Spring Security在用户登录时,自动绑定认证信息
![Image text](https://github.com/developerxiaofeng/Spring-Security/blob/master/src/main/resources/static/777.png)

  获取当前用户信息
    因为用户信息是与当前线程绑定,所以在程序的任何地方都可以使用静态的方法获取用户信息,
    
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
    if (principal instanceof UserDetails) {
    String username = ((UserDetails)principal).getUsername();
    } else {
    String username = principal.toString();
    }
   getAuthentication()返回认证信息,再次getPrincipal()返回身份信息,UserDetails便是Spring
 对身份信息封装
2.2 Authentication 接口

    public interface Authentication extends Principal, Serializable {
    //权限信息列表,默认是GrantedAuthority接口的实现类,通常代表权限信息的一系列字符串
        Collection<? extends GrantedAuthority> getAuthorities();
    //密码信息,用于用户输入的密码字符串,在认证过后通常会被移除,用于保障安全
        Object getCredentials();
    //细节信息,web应用中的实现接口通常为WebAuthenticationDetails,他记录访问者的ip地址和sessionId的值
        Object getDetails();
    //重要的 身份信息,通常返回UserDetails接口的实现类
        Object getPrincipal();
    
        boolean isAuthenticated();
    
        void setAuthenticated(boolean var1) throws IllegalArgumentException;
    }
   Authentication是Spring security包中的接口,直接继承Principal类,而Principal是位于 java.security包中
  可见Authentication是spring security中最高级别的身份/认证的抽象

Spring Security是如何完成身份验证的?
① 用户名和密码被过滤器获取到,封装成Authentication,通常情况下是 UsernamePasswordAuthenticationToken 这个实现类
② AuthenticationManager  身份管理器负责验证这个Authentication
③ 验证成功后,AuthenticationManager身份管理器器返回被充满信息的Authentication实例
 (包括上面提到的权限信息,身份信息,细节信息,但密码通常会被移除)
④ SecurityContextHolder 安全的上下文容器将第三步的充满信息的Authentication,通过SecurityContextHolder.
getContext.setAuthentication(***)方法设置其中
这是一个抽象的过程,这个过程只有Authentication没有接触过

2.3 AuthenticationManager
   接触Spring Security 的朋友相信会被AuthenticationManager,ProviderManager,AuthenticationProvider这三个类搞混
让我们稍微梳理一下之间的关系,就可以明白他们之间的联系和设计者的用意,AuthenticationManager(接口)的常用的实现类
ProviderManager内部维护一个List<AuthenticationProvider> 列表,这里边存放多种认证方式,这种设计模式其实是委托者
设计模式(Delegate)的应用,而这个列表存放着多种认证的方式,也就是说,核心的认证入口只有一个:AuthenticationManager,
不同的认证方式有:用户名+面(UsernamePasswordAuthenticationToken),邮箱+密码,手机号+密码 对应三种AuthenticationProvider
熟悉shiro的,可以把AuthenticationProvider理解成Realm,在默认的策略下,只需通过一个AuthenticationProvider的认证,即可被认证登陆成功
    只保留关键认证部分的ProvicerManager源码

    public class ProviderManager implements AuthenticationManager, MessageSourceAware,
    		InitializingBean {
    
        // 维护一个AuthenticationProvider列表
        private List<AuthenticationProvider> providers = Collections.emptyList();
              
        public Authentication authenticate(Authentication authentication)
              throws AuthenticationException {
           Class<? extends Authentication> toTest = authentication.getClass();
           AuthenticationException lastException = null;
           Authentication result = null;
    
           // 依次认证
           for (AuthenticationProvider provider : getProviders()) {
              if (!provider.supports(toTest)) {
                 continue;
              }
              try {
                 result = provider.authenticate(authentication);
    
                 if (result != null) {
                    copyDetails(authentication, result);
                    break;
                 }
              }
              ...
              catch (AuthenticationException e) {
                 lastException = e;
              }
           }
           // 如果有Authentication信息，则直接返回
           if (result != null) {
    			if (eraseCredentialsAfterAuthentication
    					&& (result instanceof CredentialsContainer)) {
                  	 //移除密码
    				((CredentialsContainer) result).eraseCredentials();
    			}
                 //发布登录成功事件
    			eventPublisher.publishAuthenticationSuccess(result);
    			return result;
    	   }
    	   ...
           //执行到此，说明没有认证成功，包装异常信息
           if (lastException == null) {
              lastException = new ProviderNotFoundException(messages.getMessage(
                    "ProviderManager.providerNotFound",
                    new Object[] { toTest.getName() },
                    "No AuthenticationProvider found for {0}"));
           }
           prepareException(lastException, authentication);
           throw lastException;
        }
    }
    
 ProviderManager中list,会依照次序去验证,认证成功就立即返回,若验证失败返回null,下一个AuthenticationProvider会继续尝试认证,如果所有的
 ProviderManager会抛出ProviderNotFoundException异常,讲到这里除了AuthticationProvider实现细节以及安全相关的过滤器没有讲,其他认证相关的
 的核心类基本介绍完毕
 
 2.4 DaoAuthenticationProvicer
 AuthenticationProvider最常用的实现就是DaoAuthenticationProvider,Dao正是数据访问层的缩写,也暗示这个身份认证器的实现思路,下面就是其UML类图
![Image text](https://github.com/developerxiaofeng/Spring-Security/blob/master/src/main/resources/static/111.png)

重新捋一下security认证过程的思路,用户前台提交用户名和密码,而数据库也是保存用户名和密码信息,认证的过程就是做个比较,而用户名和密码保存到
UsernamePasswordAuthenticationToken,而根据用户名加载用户任务则是交给了UserDetailsService


1.5  UserDetails和UserDetailsService
上面提到的UserDetails这个几口,他代表最详细用户信息,这个接口涵盖了一些必要用户信息字段,具体的实现类对他进行扩展

    public interface UserDetails extends Serializable {
    
       Collection<? extends GrantedAuthority> getAuthorities();
    
       String getPassword();
    
       String getUsername();
    
       boolean isAccountNonExpired();
    
       boolean isAccountNonLocked();
    
       boolean isCredentialsNonExpired();
    
       boolean isEnabled();
    }
    
  他和Authentication接口很类似,都拥有username,authorities,区别他们也是我们重点解释的部分,Authentication.getCredentials()与
 UserDetails.getPassword(),需要区别对待,前者是用户提交的密码凭证,后者是正确的密码,
UserDetailsService和AuthenticationProvider两者的职责常常被人们搞混，关于他们的问题在文档的FAQ和issues中屡见不鲜。记住一点即
可，敲黑板！！！UserDetailsService只负责从特定的地方（通常是数据库）加载用户信息，仅此而已，记住这一点，可以避免走很多弯路。
UserDetailsService常见的实现类有JdbcDaoImpl，InMemoryUserDetailsManager，前者从数据库加载用户，后者从内存中加载用户，也
可以自己实现UserDetailsService，通常这更加灵活。
2.6 架构概括图
![Image text](https://github.com/developerxiaofeng/Spring-Security/blob/master/src/main/resources/static/222.png)

这部分欠缺的内容有过滤器拦截表单封装成UsernamePasswordAuthenticationToken


