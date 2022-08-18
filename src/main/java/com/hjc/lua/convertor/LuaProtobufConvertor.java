package com.hjc.lua.convertor;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.hjc.lua.exception.LuaException;
import com.hjc.util.enumutil.EnumUtil;
import com.hjc.util.enumutil.IndexedEnum;
import org.apache.commons.collections4.MapUtils;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * lua - proto转换器
 *
 * @ClassName LuaProtoConverter
 * @Description lua协议转换
 * @Author hejincheng
 * @Date 2021/12/8 19:20
 * @Version 1.0
 **/
public class LuaProtobufConvertor {

    private static Logger log = LoggerFactory.getLogger(LuaProtobufConvertor.class);

    /**
     * proto msg消息转换成luaTable
     *
     * @param msg proto消息
     * @return 转换后的luaTable
     */
    public static LuaTable convertProtoToLuaTable(GeneratedMessage msg) throws LuaException {
        LuaTable luaTable = new LuaTable();
        Map<Descriptors.FieldDescriptor, Object> fieldMap = msg.getAllFields();
        if (MapUtils.isEmpty(fieldMap)) {
            return luaTable;
        }
        for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : fieldMap.entrySet()) {
            Descriptors.FieldDescriptor key = entry.getKey();

            // 当前支持的数据类型
            DataType dataType = DataType.valueOf(key.getJavaType().ordinal());
            if (dataType == null) {
                // 不支持该转换类型
                LuaException luaException = new LuaException(String.format("lua converter not support data type : %s", key.getJavaType()));
                log.error("msg.convert.to.lua.err", luaException);
                throw luaException;
            }
            LuaValue luaValue;
            if (key.isRepeated()) {
                // list数据
                List<?> repeatList = (List<?>) entry.getValue();
                // 创建list同等大小的table
                LuaTable luaArray = LuaTable.tableOf(repeatList.size(), 0);
                for (int index = 0; index < repeatList.size(); index++) {
                    Object value = repeatList.get(index);
                    // 转换LuaValue
                    LuaValue listValue = dataType.castObjectLuaValue(value);
                    if (listValue != null) {
                        // 坑！！！lua从1开始，luaj也是，insert 0会按hash插入，
                        luaArray.insert(index + 1, listValue);
                    }
                }
                luaValue = luaArray;
            } else {
                // 直接转换LuaValue
                luaValue = dataType.castObjectLuaValue(entry.getValue());
            }
            if (luaValue != null) {
                // lua有值才set到table中
                luaTable.set(key.getName(), luaValue);
            }
        }
        return luaTable;
    }

    /**
     * luaTable转换成proto msg
     *
     * @param msgClz   消息对象
     * @param luaTable 要转换的LuaTable
     * @param <T>      proto类（需要包含Builder）
     * @return proto msg
     */
    public static <T extends GeneratedMessage> T convertLuaTableToProto(Class<T> msgClz, LuaTable luaTable)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, LuaException {
        if (luaTable == null) {
            throw new LuaException(String.format("%s convert error : luaTable is null", msgClz));
        }
        Message.Builder builder = getClassBuilder(msgClz);
        return (T) convertLuaTableToProto(builder, luaTable);
    }

    private static Message.Builder getClassBuilder(Class<?> msgClz)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method newBuilderMethod = msgClz.getMethod("newBuilder");
        return (Message.Builder) newBuilderMethod.invoke(null);
    }

    private static GeneratedMessage convertLuaTableToProto(Message.Builder builder, LuaTable luaTable)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, LuaException {
        for (Descriptors.FieldDescriptor field : builder.getDescriptorForType().getFields()) {

            String fieldName = field.getName();
            LuaValue luaValue = luaTable.get(fieldName);
            Class<?> fieldClass = builder.getField(field).getClass();

            // 当前支持的数据类型
            DataType dataType = DataType.valueOf(field.getJavaType().ordinal());
            if (dataType == null) {
                // 不支持该转换类型
                LuaException luaException = new LuaException(String.format("lua converter not support data type : %s", field.getJavaType()));
                log.error("lua.convert.to.msg.err", luaException);
                throw luaException;
            }
            Object object;
            if (field.isRepeated()) {
                // list数据
                List<Object> fieldList = null;
                if (luaValue != LuaValue.NIL) {
                    LuaTable repeatLuaTables = (LuaTable) luaValue;
                    int tableLen = repeatLuaTables.rawlen();
                    // 创建table同等大小的list
                    fieldList = new ArrayList<>(tableLen);
                    for (int i = 1; i <= tableLen; i++) {
                        LuaValue fieldLuaValue = repeatLuaTables.get(i);
                        // 转换java类
                        Message.Builder fieldBuilder = dataType.getFieldBuilder(builder, field);
                        Object javaObject = dataType.castLuaValueToObject(fieldBuilder, fieldLuaValue);
                        if (javaObject != null) {
                            fieldList.add(javaObject);
                        }
                    }
                } else {
                    fieldList = new ArrayList<>(0);
                }
                object = fieldList;
            } else {
                // 直接转换java类
                Message.Builder fieldBuilder = null;
                if (field.getJavaType().ordinal() == DataType.MESSAGE.getIndex()) {
                    // MESSAGE类型需要消息体Builder
                    fieldBuilder = getClassBuilder(fieldClass);
                }
                object = dataType.castLuaValueToObject(fieldBuilder, luaValue);
            }
            if (object != null) {
                builder.setField(field, object);
            }
        }
        return (GeneratedMessage) builder.build();
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
        INT(Descriptors.FieldDescriptor.JavaType.INT, int.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value) {
                return LuaValue.valueOf((int) value);
            }

            @Override
            public Object castLuaValueToObject(Message.Builder builder, LuaValue value) {
                return value.toint();
            }
        },
        // long
        LONG(Descriptors.FieldDescriptor.JavaType.LONG, long.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value) {
                return LuaValue.valueOf((double) (long) value);
            }

            @Override
            public Object castLuaValueToObject(Message.Builder builder, LuaValue value) {
                return value.tolong();
            }
        },
        // float
        FLOAT(Descriptors.FieldDescriptor.JavaType.FLOAT, float.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value) {
                return LuaValue.valueOf((float) value);
            }

            @Override
            public Object castLuaValueToObject(Message.Builder builder, LuaValue value) {
                return value.tofloat();
            }
        },
        // double
        DOUBLE(Descriptors.FieldDescriptor.JavaType.DOUBLE, double.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value) {
                return LuaValue.valueOf((double) value);
            }

            @Override
            public Object castLuaValueToObject(Message.Builder builder, LuaValue value) {
                return value.todouble();
            }
        },
        // bool
        BOOLEAN(Descriptors.FieldDescriptor.JavaType.BOOLEAN, boolean.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value) {
                return LuaBoolean.valueOf(Boolean.parseBoolean(value.toString()));
            }

            @Override
            public Object castLuaValueToObject(Message.Builder builder, LuaValue value) {
                return value.toboolean();
            }
        },
        // string
        STRING(Descriptors.FieldDescriptor.JavaType.STRING, Object.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value) {
                return LuaValue.valueOf(value.toString());
            }

            @Override
            public Object castLuaValueToObject(Message.Builder builder, LuaValue value) {
                return value.toString();
            }
        },
        // bytes
        BYTE_STRING(Descriptors.FieldDescriptor.JavaType.BYTE_STRING, ByteString.class),
        // enum
        ENUM(Descriptors.FieldDescriptor.JavaType.ENUM, Enum.class),
        // message
        MESSAGE(Descriptors.FieldDescriptor.JavaType.MESSAGE, GeneratedMessage.class) {
            @Override
            public LuaValue castObjectLuaValue(Object value) throws LuaException {
                // 递归转换子table
                return LuaProtobufConvertor.convertProtoToLuaTable((GeneratedMessage) value);
            }

            @Override
            public Object castLuaValueToObject(Message.Builder builder, LuaValue value)
                    throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, LuaException {
                // 递归转换子对象
                if (!value.istable()) {
                    return null;
                }
                return LuaProtobufConvertor.convertLuaTableToProto(builder, (LuaTable) value);
            }

            @Override
            public Message.Builder getFieldBuilder(Message.Builder builder, Descriptors.FieldDescriptor field) {
                return builder.newBuilderForField(field);
            }
        },
        ;

        private Descriptors.FieldDescriptor.JavaType javaType;
        private Class<?> javaClass;

        DataType(Descriptors.FieldDescriptor.JavaType javaType, Class<?> javaClass) {
            this.javaType = javaType;
            this.javaClass = javaClass;
        }

        @Override
        public int getIndex() {
            return this.javaType.ordinal();
        }

        private static final List<DataType> values = IndexedEnum.IndexedEnumUtil.toIndexes(DataType.values());

        private static final Map<Class<?>, DataType> classValueMap = new HashMap<>();

        static {
            for (DataType dataType : values) {
                if (dataType == null) {
                    continue;
                }
                classValueMap.put(dataType.javaClass, dataType);
            }
        }

        public static List<DataType> getValues() {
            return values;
        }

        public static DataType valueOf(int value) {
            return EnumUtil.valueOf(values, value);
        }

        public static DataType valueOf(Class<?> javaClass) {
            return classValueMap.get(javaClass);
        }

        /**
         * java对象转换成luaTable
         *
         * @param value java对象
         * @return luaTable
         */
        public LuaValue castObjectLuaValue(Object value) throws LuaException {
            return null;
        }

        /**
         * luaTable转换成java对象
         *
         * @param builder 新对象builder（仅MESSAGE类型有用，其他传null）
         * @param value   lua对象
         * @return java对象
         */
        public Object castLuaValueToObject(Message.Builder builder, LuaValue value) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, LuaException {
            return null;
        }

        /**
         * 获取属性的builder
         * <b>只有MESSAGE类型才有实现</b>
         *
         * @param builder 父类型builder
         * @param field   属性
         * @return 属性的builder
         */
        public Message.Builder getFieldBuilder(Message.Builder builder, Descriptors.FieldDescriptor field) {
            return null;
        }
    }
}
