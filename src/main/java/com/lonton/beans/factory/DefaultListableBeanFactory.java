package com.lonton.beans.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lonton.beans.config.BeanDefinition;
import com.lonton.beans.factory.support.BeanDefinitionRegistry;
import com.lonton.core.io.FileSystemResource;
import com.lonton.core.io.Resource;
import com.lonton.core.io.XmlBeanDefinitionReader;
import com.lonton.exception.BeansException;
import com.lonton.exception.NoSuchBeanDefinitionException;

/*
 * @author chenwentao
 * @since  2017-01-25
 * 
 * 1.一個基本的容器實現,我這裡簡單實現，直接繼承AbstractBeanFactory
 * 这个工厂和XmlBeanDefinitionReader是联系在一起的，当调用XmlBeanDefinitionReader类
 * 中的loadBeanDefinitions()方法时，会调用registerBeanDefinition()方法，讲beandefinition
 * 注入到DefaultListableBeanFactory，后面我在拓展工厂的时候，只需要继承DefaultListableBeanFactory
 * 就拥有了完整的beandefinition集合
 * 3.这里我们只能加载FileSystemResource，如需拓展，继承此类就可以
 * 
 */
public class DefaultListableBeanFactory extends AbstractBeanFactory
        implements BeanDefinitionRegistry, ResourceLoaderBeanFactory {

    private static Logger log = LoggerFactory.getLogger(DefaultListableBeanFactory.class);
    protected Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>(256);
    protected Resource resource;
    static {
        PropertyConfigurator.configure("log4j.properties");
    }

    /*
     * 当然了，我们所使用的ioc都是具有加载资源的能力的,所以给定两个基本的构造方法 你可以给工厂直接注入资源，当然也可以直接注入资源地址，因为它是具备资源加载的能力的
     */
    public DefaultListableBeanFactory(Resource resource) throws Exception {
        this.resource = resource;
        refresh();
    }

    public DefaultListableBeanFactory(String location) throws Exception {
        this.resource = getResource(location);
        refresh();
    }

    /*
     * 资源的问题解决了，我们还得具备读取beandefinition的能力,本来是应该继承的容器中就具有了 读取的能力，这里为了简便，我们使用内部类实现
     */
    protected class ResourceReaderBeanFactory extends XmlBeanDefinitionReader {
        public ResourceReaderBeanFactory(BeanDefinitionRegistry registry) {
            super(registry);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.lonton.beans.factory.support.BeanDefinitionRegistry#registerBeanDefinition(java.lang.String,
     * com.lonton.beans.config.BeanDefinition) 在这里我们已经获取到了beanName and beanDefinition，只需将其注入到Map集合中就行了
     */
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        // 这里我就不对这两个参数进行验证了，直接将其put进map集合
        // System.out.println("正在注册"+beanName+beanDefinition);
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    @Override
    public void removeBeanDefinition(String beanName) {
        if (beanDefinitionMap.get(beanName) != null) {
            beanDefinitionMap.remove(beanName);
        } else {
            log.warn("需要移除的bean不存在！");
        }
    }

    // 给定一个地址
    @Override
    public Resource getResource(String location) {
        // 这里因为是通过地址获取资源，所以我们返回一个FileSystemResource
        return new FileSystemResource(location);
    }

    // 好了，最后给定一个初始化方法
    protected void refresh() throws Exception {
        // 在这里，我们完成容器的初始化
        // 1.资源我们已经在构造方法中获取
        // 2.资源的解析
        int count = new ResourceReaderBeanFactory(this).loadBeanDefinitions(resource);
        // 3.容器的注册方法会被自动调用，此时注册就完成了
        log.info("一共初始化了" + count + "个bean");
    }

    @Override
    public Object getBean(String name) {
        //
        Object object = beanDefinitionMap.get(name).getObject();
        if (object == null) {
            try {
                throw new NoSuchBeanDefinitionException("");
            } catch (NoSuchBeanDefinitionException e) {
                log.error("找不到匹配的bean");
            }
        }
        return object;
    }

    /*
     * (non-Javadoc)
     * @see com.lonton.beans.factory.AbstractBeanFactory#getBean(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        Object object = null;
        try {
            object = getBean(name);
        } catch (Exception e) {
            log.error("无法获取bean");
        }
        // 加入类型判断，如不符合，抛出异常
        if (requiredType.isInstance(object)) {
            return (T) object;
        } else {
            log.error("bean类型不匹配");
            throw new BeansException();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.lonton.beans.factory.AbstractBeanFactory#isSingleton(java.lang.String)
     */
    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        // TODO
        return super.isSingleton(name);
    }
}
