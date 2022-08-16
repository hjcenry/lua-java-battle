package com.hjc.lua.convertor;

import com.hjc.lua.exception.LuaException;
import com.hjc.util.enumutil.EnumUtil;
import com.hjc.util.enumutil.IndexedEnum;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * lua和javaBean互相转换的转换器
 *
 * @ClassName LuaJavaBeanConverter
 * @Description
 * @Author hejincheng
 * @Date 2022/1/6 11:48
 * @Version 1.0
 **/
public class LuaJavaBeanConvertor {

    /**
     * JavaBean对象转换LuaTable
     *
     * @param bean JavaBean对象
     * @return LuaTable
     */
    public static LuaTable convertJavaBeanToLuaTable(Object bean) throws IllegalAccessException, LuaException {
        if (bean == null) {
            return new LuaTable(0, 0);
        }
        Class<?> clz = bean.getClass();
        Field[] fields = clz.getDeclaredFields();

        LuaTable luaTable = new LuaTable();
        for (Field field : fields) {
            // 取出值
            field.setAccessible(true);
            Object fieldValue = field.get(bean);
            field.setAccessible(false);
            LuaValue luaValue = convertFieldToLuaValue(field.getType(), bean.getClass().getSimpleName(), fieldValue);
            if (luaValue != null) {
                luaTable.set(field.getName(), luaValue);
            }
        }
        return luaTable;
    }

    /**
     * 转换字段为LuaValue
     *
     * @param fieldClz 字段类
     * @param beanName bean名
     * @param obj      对象
     * @return LuaValue
     */
    private static LuaValue convertFieldToLuaValue(Class<?> fieldClz, String beanName, Object obj) throws LuaException, IllegalAccessException {
        //  数据类型
        DataType dataType = DataType.valueOf(fieldClz);
        if (dataType == null) {
            // 找不到这个类型，递归处理它
            return convertJavaBeanToLuaTable(obj);
        }

        if (List.class.isAssignableFrom(fieldClz)) {
            if (obj == null) {
                return new LuaTable(0, 0);
            }
            // list数据
            List<?> list = (List<?>) obj;
            // 创建list同等大小的table
            LuaTable luaArray = LuaTable.tableOf(list.size(), 0);
            for (int index = 0; index < list.size(); index++) {
                Object dataObj = list.get(index);
                // 转换LuaValue
                LuaValue listValue = dataType.castObjectLuaValue(dataObj, beanName);
                if (listValue != null) {
                    luaArray.insert(index + 1, listValue);
                }
            }
            return luaArray;
        }

        if (obj == null) {
            return LuaValue.NIL;
        }
        return dataType.castObjectLuaValue(obj, beanName);
    }

    /**
     * 数据类型
     *
     * @ClassName LuaProtoConverter
     * @Author hejincheng
     * @Date 2021/12/9 11:12
     * @Version 1.0
     **/
    enum DataType implements IndexedEnum {
        // int
        INT(0, int.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value, String beanName) {
                return LuaValue.valueOf((int) value);
            }
        },
        INT_BOX(1, Integer.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value, String beanName) {
                return LuaValue.valueOf((Integer) value);
            }
        },
        // long
        LONG(2, long.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value, String beanName) {
                return LuaValue.valueOf((double) (long) value);
            }
        },
        LONG_BOX(3, Long.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value, String beanName) {
                return LuaValue.valueOf((double) (Long) value);
            }
        },
        // float
        FLOAT(4, float.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value, String beanName) {
                return LuaValue.valueOf((float) value);
            }
        },
        FLOAT_BOX(5, Float.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value, String beanName) {
                return LuaValue.valueOf((Float) value);
            }
        },
        // double
        DOUBLE(6, double.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value, String beanName) {
                return LuaValue.valueOf((double) value);
            }
        },
        DOUBLE_BOX(7, Double.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value, String beanName) {
                return LuaValue.valueOf((Double) value);
            }
        },
        // bool
        BOOLEAN(8, boolean.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value, String beanName) {
                return LuaValue.valueOf((boolean) value);
            }
        },
        BOOLEAN_BOX(9, Boolean.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value, String beanName) {
                return LuaValue.valueOf((Boolean) value);
            }
        },
        // string
        STRING(10, String.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value, String beanName) {
                if (value == null) {
                    return LuaValue.EMPTYSTRING;
                }
                return LuaValue.valueOf(value.toString());
            }
        },
        // list
        ARRAY_LIST(11, ArrayList.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value, String beanName) throws IllegalAccessException, LuaException {
                if (value == null) {
                    return LuaValue.EMPTYSTRING;
                }
                // 递归转换子table
                return convertFieldToLuaValue(value.getClass(), beanName, value);
            }
        },
        // enum
        ENUM(12, Enum.class),
        ;

        private Class<?> javaClass;
        private int index;

        DataType(int index, Class<?> javaClass) {
            this.javaClass = javaClass;
            this.index = index;
        }

        @Override
        public int getIndex() {
            return this.index;
        }

        private static final List<DataType> values = IndexedEnum.IndexedEnumUtil.toIndexes(DataType.values());

        private static final Map<Class<?>, DataType> CLASS_VALUE_MAP = new HashMap<>();

        static {
            for (DataType dataType : values) {
                if (dataType == null) {
                    continue;
                }
                CLASS_VALUE_MAP.put(dataType.javaClass, dataType);
            }
        }

        public static List<DataType> getValues() {
            return values;
        }

        public static DataType valueOf(int value) {
            return EnumUtil.valueOf(values, value);
        }

        public static DataType valueOf(Class<?> javaClass) {
            return CLASS_VALUE_MAP.get(javaClass);
        }

        /**
         * java对象转换成luaTable
         *
         * @param value    java对象
         * @param beanName
         * @return
         */
        public LuaValue castObjectLuaValue(Object value, String beanName) throws IllegalAccessException, LuaException {
            return null;
        }
    }
}
