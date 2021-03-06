package com.lonton.beans.factory.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import com.lonton.exception.XmlConfigurationErrorException;
/*
 * @author chenwentao
 * @since  2017-01-25
 */
//解析方法，返回BeanDefinition對象集合
public class XmlParser{
	
  private static Map<String, Object> beans = new HashMap<String, Object>();
  private static List<String> ComponentPackageNames=new ArrayList<String>();
  
	public final static Map<String, Object> parser(Document doc) throws Exception{
		Element root = doc.getRootElement();// 获得根节点
		@SuppressWarnings("unchecked")
		List<Element> list = root.getChildren();
		for (Element elements : list){
			String PackageName=elements.getAttributeValue("packagename");
			if(PackageName!=null){		
				ComponentPackageNames.add(PackageName);
			}
			// 获取属性值，即为对象的名字
			String ObjectName = elements.getAttributeValue("id");
			// 在获取类的路径，在通过java反射获取类的类类型，在获取该类的对象
			String classpath = elements.getAttributeValue("class");
			Object obj=null;
			if(classpath!=null && ObjectName!=null){
				obj = Class.forName(classpath).newInstance();
				beans.put(ObjectName, obj);
			}
			// 获取子节点下的property节点
			@SuppressWarnings("unchecked")
			List<Element> eles = elements.getChildren("property");
			// 进行遍历
			for (Element e : eles) {
				String proName = e.getAttributeValue("name");
				Object beanObj = getBean(e.getAttributeValue("ref"));
				String str=e.getAttributeValue("value");
				if(beanObj==null && str!=null){
					beanObj=str;
					beanObj=(String)beanObj;
				}
				if(beanObj==null && str==null){
					throw new XmlConfigurationErrorException("At XmlParser");
				}
				//System.out.println(beanObj.toString());
				// 然后调用构造方法
				String methodName = "set"
						+ proName.substring(0, 1).toUpperCase()
						+ proName.substring(1);
				// 通过反射获取方法
				Method method = obj.getClass().getMethod(methodName,
						beanObj.getClass());
				method.invoke(obj,beanObj);
			}
		}
		return beans;
	}
	
	public static Object getBean(String name) {
		return beans.get(name);
	}
	//获取配置文件中的包名
	public static List<String> getComponentPackageNames() {
		return ComponentPackageNames;
	}
}
