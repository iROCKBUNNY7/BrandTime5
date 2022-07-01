package com.jfireframework.jfire.cache.el;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.verify.Verify;

public class Jel
{
    public static String createVarIf(String conditionStatment, String[] paramNames, Class<?>[] types) throws NoSuchFieldException, SecurityException
    {
        conditionStatment = conditionStatment.trim();
        StringCache cache = new StringCache();
        char c;
        int flag = 0;
        InvokeNameAndType invokeNameAndType = null;
        String condition = null;
        String param = null;
        // 表达式的写法只支持变量在前，参数在后。也就是说只能写$user.age >24 而不能写24<$user.age
        while (flag < conditionStatment.length())
        {
            c = conditionStatment.charAt(flag);
            if (c == '$')
            {
                int varStart = flag + 1;
                flag = getEndFlag(conditionStatment, flag);
                String var = null;
                if (flag < conditionStatment.length() - 1 && conditionStatment.charAt(flag) == '(' && conditionStatment.charAt(flag + 1) == ')')
                {
                    flag += 2;
                    var = conditionStatment.substring(varStart, flag);
                }
                else if (flag < conditionStatment.length() - 1 && conditionStatment.charAt(flag) == '(')
                {
                    throw new IllegalArgumentException(StringUtil.format("动态sql功能只支持无参方法，请检查{}", conditionStatment));
                }
                else
                {
                    var = conditionStatment.substring(varStart, flag);
                }
                invokeNameAndType = buildParam(var, paramNames, types);
                continue;
            }
            else if (c == '>' || c == '<' || c == '!' || c == '=')
            {
                if (conditionStatment.charAt(flag + 1) == '=')
                {
                    condition = String.valueOf(c + "=");
                    flag += 2;
                    continue;
                }
                else
                {
                    condition = String.valueOf(c);
                    flag++;
                    continue;
                }
            }
            else if (c == ' ' || c == '(' || c == ')' || c == '|' || c == '&')
            {
                // 在遇到(,||,&& 时代表条件的结束。此时可以生成一个表达式
                if (invokeNameAndType != null && c != ' ' && c != ')' && flag < conditionStatment.length() - 1)
                {
                    if (c == '(' || conditionStatment.charAt(flag + 1) == '|' || conditionStatment.charAt(flag + 1) == '&')
                    {
                        if (param == null && condition == null)
                        {
                            createStatement("null", cache, invokeNameAndType.getInvokeName(), invokeNameAndType.getReturnType(), "!=");
                        }
                        else
                        {
                            createStatement(param, cache, invokeNameAndType.getInvokeName(), invokeNameAndType.getReturnType(), condition);
                        }
                        cache.append(' ');
                        invokeNameAndType = null;
                        condition = null;
                        param = null;
                    }
                }
                cache.append(c);
                flag++;
                continue;
            }
            else if (c == '\'')
            {
                int end = conditionStatment.indexOf('\'', flag);
                param = conditionStatment.substring(flag + 1, end);
                createStatement(param, cache, invokeNameAndType.getInvokeName(), invokeNameAndType.getReturnType(), condition);
                invokeNameAndType = null;
                condition = null;
                param = null;
                flag = end + 1;
                continue;
            }
            // 如果都不是上面的那些字符，就意味着可能是数字或者是布尔值。（在输入正确的情况下，故意输错不说。）
            else
            {
                Verify.notNull(invokeNameAndType, "sql语句错误，请检查是否条件判断'{}'前面是否缺少了$", conditionStatment);
                int paramStart = flag;
                flag = getEndFlag(conditionStatment, flag);
                param = conditionStatment.substring(paramStart, flag);
                createStatement(param, cache, invokeNameAndType.getInvokeName(), invokeNameAndType.getReturnType(), condition);
                invokeNameAndType = null;
                condition = null;
                param = null;
                continue;
            }
        }
        if (invokeNameAndType != null && condition == null && param == null)
        {
            createStatement("null", cache, invokeNameAndType.getInvokeName(), invokeNameAndType.getReturnType(), "!=");
        }
        return cache.toString();
    }
    
    private static int getParamNameIndex(String inject, String[] paramNames)
    {
        for (int i = 0; i < paramNames.length; i++)
        {
            if (paramNames[i].equals(inject))
            {
                return i;
            }
        }
        throw new RuntimeException("给定的参数" + inject + "不在参数列表中");
    }
    
    private static InvokeNameAndType buildParam(String inject, String[] paramNames, Class<?>[] paramTypes) throws NoSuchFieldException, SecurityException
    {
        
        if (inject.indexOf('.') == -1)
        {
            int index = getParamNameIndex(inject, paramNames);
            String result = "";
            result += "$" + (index + 1);
            InvokeNameAndType invokeNameAndType = new InvokeNameAndType(result, paramTypes[index]);
            return invokeNameAndType;
        }
        else
        {
            String[] tmp = inject.split("\\.");
            int index = getParamNameIndex(tmp[0], paramNames);
            Object[] returns = ReflectUtil.getBuildMethodAndType(inject, paramTypes[index]);
            Class<?> returnType = (Class<?>) returns[1];
            String result = "";
            result += "$" + (index + 1) + returns[0];
            InvokeNameAndType invokeNameAndType = new InvokeNameAndType(result, returnType);
            return invokeNameAndType;
        }
    }
    
    /**
     * 创建一个条件判断，使用变量名，条件，参数三个属性。并且将生成的条件判断加入到formatsql中。
     * 
     * @param param
     * @param formatSql
     * @param transVar
     * @param varType
     * @param condition
     */
    private static void createStatement(String param, StringCache formatSql, String transVar, Class<?> varType, String condition)
    {
        // 如果是user.name，需要判断user！=null 并且user.getName() != null。必须逐层验证
        int flag = 0;
        formatSql.append(" (");
        while ((flag = transVar.indexOf('.', flag)) != -1)
        {
            formatSql.append("($w)").append(transVar.substring(0, flag)).append(" != null && ");
            flag++;
        }
        if (param != null && param.equals("null"))
        {
            if (condition.equals("==") || condition.equals("!="))
            {
                formatSql.append(transVar).append(" ").append(condition).append(" null )");
                return;
            }
            else
            {
                throw new RuntimeException(StringUtil.format("条件语句存在错误，参数为null时，条件只能是'='或'!='"));
            }
        }
        if (varType == null)
        {
            formatSql.append(transVar);
            if (condition == null)
            {
                formatSql.append("==true )");
            }
            else
            {
                formatSql.append(condition).append(param).append(" )");
            }
            return;
        }
        if (varType.isPrimitive())
        {
            if (varType == char.class)
            {
                formatSql.append(transVar).append(condition).append("'").append(param).append("' )");
                
            }
            else
            {
                formatSql.append(transVar).append(condition).append(param).append(" )");
            }
            return;
        }
        formatSql.append(transVar).append(" != null && ");
        if (varType == String.class)
        {
            if (condition.equals("=="))
            {
                formatSql.append(transVar).append(".equals(\"").append(param).append("\") )");
            }
            else if (condition.equals("!="))
            {
                formatSql.append(transVar).append(".equals(\"").append(param).append("\")==false )");
            }
        }
        else if (varType == Integer.class)
        {
            formatSql.append(transVar).append(".intValue() ").append(condition).append(param).append(" )");
        }
        else if (varType == Long.class)
        {
            formatSql.append(transVar).append(".longValue() ").append(condition).append(param).append(" )");
        }
        else if (varType == Short.class)
        {
            formatSql.append(transVar).append(".shortValue() ").append(condition).append(param).append(" )");
        }
        else if (varType == Double.class)
        {
            formatSql.append(transVar).append(".doubleValue() ").append(condition).append(param).append(" )");
        }
        else if (varType == Float.class)
        {
            formatSql.append(transVar).append(".floatValue() ").append(condition).append(param).append(" )");
        }
        else if (varType == Long.class)
        {
            formatSql.append(transVar).append(".longValue() ").append(condition).append(param).append(" )");
        }
        else
        {
            throw new RuntimeException("不能识别的处理类型" + varType);
        }
    }
    
    private static int getEndFlag(String sql, int start)
    {
        while (start < sql.length())
        {
            char c = sql.charAt(start);
            if (c == '>' || c == '<' || c == '!' || c == '=' || c == ' ' || c == ',' //
                    || c == '#' || c == '+' || c == '-' || c == '(' || c == ')' || c == ']' || c == '[')
            {
                break;
            }
            start++;
        }
        return start;
    }
    
    private static class InvokeNameAndType
    {
        private final String   invokeName;
        private final Class<?> returnType;
        
        public InvokeNameAndType(String invokeName, Class<?> returnType)
        {
            this.invokeName = invokeName;
            this.returnType = returnType;
        }
        
        public String getInvokeName()
        {
            return invokeName;
        }
        
        public Class<?> getReturnType()
        {
            return returnType;
        }
        
    }
    
    /**
     * 构造一个值表达式。比如'abc'+$user.name
     * 那么就会被转化为"abc"+user.getName(),并且user会被替换为正确的，如$1这样的表达。以方便在javassist中使用
     * 
     * @param expression
     * @param names
     * @param types
     * @return
     * @throws JelException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    public static String createValue(String expression, String[] names, Class<?>[] types) throws NoSuchFieldException, SecurityException
    {
        StringCache cache = new StringCache();
        int length = expression.length();
        int index = 0;
        while (index < length)
        {
            char c = expression.charAt(index);
            if (c == '$')
            {
                int end = getEndFlag(expression, index);
                String value;
                value = expression.substring(index + 1, end);
                cache.append(buildParam(value, names, types).getInvokeName());
                index = end;
                continue;
            }
            if (c == '\'')
            {
                int end = expression.indexOf('\'', index + 1);
                if (end == -1)
                {
                    throw new UnSupportException("key的规则有问题，缺少了一边的'\"'");
                }
                cache.append('"').append(expression.substring(index, end + 1)).append('"');
                index = end + 1;
                continue;
            }
            else if (c == ' ' || c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')')
            {
                cache.append(c);
                index += 1;
                continue;
            }
            else
            {
                int end = getEndFlag(expression, index);
                if (end == -1)
                {
                    throw new UnSupportException("key的规则有问题，缺少了一边的'\"'");
                }
                cache.append(expression.substring(index, end));
                index = end;
                continue;
            }
        }
        return '(' + cache.toString() + ')';
    }
}
