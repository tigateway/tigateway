package ti.gateway.base.util;

import org.springframework.cglib.beans.BeanCopier;

import java.util.*;

/**
 * Bean Copier
 */
public class BeanCopierUtils {

    /**
     * 拷贝Bean
     *
     * @param sourceObj 目标Obj
     * @param source    原Class
     * @param target    目标Class
     * @param <S>      原类型
     * @param <T>      目标类型
     * @return 目标Obj
     */
    public static <S, T> T copierTargetBean(Object sourceObj, Class<S> source, Class<T> target) {
        if (Objects.nonNull(sourceObj)) {
            BeanCopier beanCopier = BeanCopier.create(source, target, false);
            T targetObj = null;
            try {
                targetObj = target.newInstance();
            } catch (Exception e) {
                throw new ClassCastException(sourceObj.getClass().getName() + " to " +
                        targetObj.getClass().getName() + " Class convert Exception ");
            }
            beanCopier.copy(sourceObj, targetObj, null);
            return targetObj;
        }
        return null;
    }

    /**
     * 拷贝目标List
     *
     * @param sourceObjList 目标List
     * @param source        原Class
     * @param target        目标Class
     * @param <S>          原类型
     * @param <T>          目标类型
     * @return 目标List
     */
    public static <S, T> List<T> copierTargetBeanList(List<?> sourceObjList, Class<S> source, Class<T> target) {
        if (null != sourceObjList) {
            List<T> targetList = new ArrayList<>(sourceObjList.size());
            for (Object sourceObj : sourceObjList) {
                T targetObj = copierTargetBean(sourceObj, source, target);
                if (Objects.nonNull(targetObj)) {
                    targetList.add(targetObj);
                }
            }
            return targetList;
        }
        return null;
    }

    /**
     * 拷贝目标Set
     *
     * @param sourceObjList 目标Set
     * @param source        原Class
     * @param target        目标Class
     * @param <S>         原类型
     * @param <T>         目标类型
     * @return 目标Set
     */
    public static <S, T> Set<T> copierTargetBeanSet(Set<?> sourceObjList, Class<S> source, Class<T> target) {
        if (null != sourceObjList) {
            Set<T> targetList = new HashSet<>(sourceObjList.size());
            for (Object sourceObj : sourceObjList) {
                T targetObj = copierTargetBean(sourceObj, source, target);
                if (Objects.nonNull(targetObj)) {
                    targetList.add(targetObj);
                }
            }
            return targetList;
        }
        return null;
    }

}
