### API文档:
```
java.lang.Integer
public static Integer getInteger(String nm,
                                 int val)
```
Determines the integer value of the system property with the specified name.
The first argument is treated as the name of a system property.
System properties are accessible through the System.getProperty(String) method.
The string value of this property is then interpreted as an integer value using the grammar supported by decode and an Integer object representing this value is returned.
The second argument is the default value.
An Integer object that represents the value of the second argument is returned if there is no property of the specified name, if the property does not have the correct numeric format, or if the specified name is empty or null.
In other words, this method returns an Integer object equal to the value of:
getInteger(nm, new Integer(val))
but in practice it may be implemented in a manner such as:
       Integer result = getInteger(nm, null);
       return (result == null) ? new Integer(val) : result;

to avoid the unnecessary allocation of an Integer object when the default value is not needed.
Parameters:
nm - property name.
val - default value.
Returns:
the Integer value of the property.
Throws:
SecurityException - for the same reasons as System.getProperty
See Also:
System.getProperty(String), System.getProperty(String, String)

### 中文翻译:
```
java.lang.Integer
public static Integer getInteger(String nm,
                                 int val)
```
确定指定名称的系统属性的整数值.
第一个参苏被认为系统属性的名称.
系统属性可通过System.getProperty(String)方法获取.
该属性的字符串值使用支持的语法解释为整数值.并返回表示此值的整数对象.

第二个参数是默认值.
如果这里没有指定名称的属性,或者非数字格式,再或者为空,则返回表示第二参数值的整型对象。
