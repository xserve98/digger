package com.ko30.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;

public class BeanUtils {

	private BeanUtils() {
	}

	/**
	 * 获取对象的属性Field列表
	 */
	public static List<Field> getFields(Object obj) {
		Class<?> clazz = obj.getClass();
		return getFields(clazz);
	}

	/**
	 * 获取类的属性Field列表
	 */
	public static List<Field> getFields(Class<?> clazz) {
		List<Field> list = new ArrayList<Field>();
		while (null != clazz) {
			for (Field f : clazz.getDeclaredFields()) {
				if ((!Modifier.isStatic(f.getModifiers())) && (!Modifier.isFinal(f.getModifiers()))) {
					list.add(f);
				}
			}
			clazz = clazz.getSuperclass();
		}

		return list;
	}

	/**
	 * 对象属性拷贝
	 */
	public static void copy(Object source, Object target) {
		copy(source, target, null);
	}

	/**
	 * 对象属性拷贝
	 */
	public static void copy(Object source, Object target, String[] ignoreFields) {
		Class<?> srcClazz = source.getClass();
		Class<?> tgtClazz = target.getClass();

		List<Field> srcFields = getFields(srcClazz);
		List<Field> tgtFields = getFields(tgtClazz);

		try {
			for (Field field : srcFields) {
				String fieldName = field.getName();

				if (containsString(ignoreFields, fieldName)) {
					continue;
				}

				Field tgtField = findField(tgtFields, fieldName);
				if (null != tgtField) {
					field.setAccessible(true);
					Object value = field.get(source);

					if (null != value) {
						tgtField.setAccessible(true);
						tgtField.set(target, value);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 获取对象的属性
	 */
	public static Object getProperty(Object obj, String propertyName) {
		Class<?> clazz = obj.getClass();
		String methodName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
		try {
			Method method = clazz.getMethod(methodName);
			return method.invoke(obj);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 根据名称查找Field
	 */
	private static Field findField(List<Field> fields, String fieldName) {
		for (Field f : fields) {
			if (f.getName().equals(fieldName)) {
				return f;
			}
		}

		return null;
	}

	/**
	 * 判断数组中是否包含指定字符串
	 */
	private static boolean containsString(String[] arr, String str) {
		if (null != arr) {
			for (String s : arr) {
				if (null != s && s.equals(str)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 判断是否为包装类型
	 */
	public static boolean isWrapType(Class<?> clz) {
		try {
			return ((Class<?>) clz.getField("TYPE").get(null)).isPrimitive();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判断是否为基本类型
	 */
	public static boolean isPrimitiveType(Class<?> clz) {
		try {
			return clz.isPrimitive();
		} catch (Exception e) {
			return false;
		}
	}

	@SuppressWarnings(value = { "unchecked" })
	public static Map<String, String> objectToMap(Object obj) {
		return objectToMap(obj, "");
	}
	
	@SuppressWarnings(value = { "unchecked" })
	public static Map<String, String> objectToMap(Object obj,String prefix) {
		if (obj == null)
			return null;
		ObjectMapper m = new ObjectMapper();
		return mapCollection(prefix, m.convertValue(obj, Map.class));
	}

	@SuppressWarnings(value = { "unchecked" })
	public static Map<String, String> mapCollection(String prefix, Map<String, Object> baseMap) {
		Map<String, String> map = new HashMap<String, String>();
		for (Entry<String, Object> entry : baseMap.entrySet()) {
			if (entry.getValue() != null) {
				if (entry.getValue() instanceof Map) {
					Map<String, String> subMap = mapCollection(prefix+entry.getKey() + ".",
							(Map<String, Object>) entry.getValue());
					for (Entry<String, String> subEntry : subMap.entrySet()) {
						map.put(prefix + subEntry.getKey(), subEntry.getValue());
					}
				} else {
					map.put(prefix + entry.getKey(), entry.getValue().toString());
				}
			}
		}

		return map;
	}

}
