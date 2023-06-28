package com.github.bluecatlee.dcep.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface DCEPField {

    // 是否必传
    boolean required() default false;

    // 字段名策略 默认为大写
    Strategy strategy() default Strategy.UPPER;

    // 字段名重写 字段名策略为Strategy.CUSTOM时取该字段；如果name属性有值 也会优先取该值
    String name() default "";

    // 是否参与mac计算
//    boolean mac() default false;

    // 排序序号 值越小 字段排序越靠前 小于等于0时不参与排序 注意排序值从1开始 不要定义相同
    int order() default -1;

    // true表示： 为空(null)时不参与排序
    boolean orderNonNull() default false;

    // 字符的最大长度 超过则抛出异常  -1表示不处理
    // 中文字符串的长度应该小于 maxLen / 3
//    int maxLen() default -1;

    // 字符串值的最大长度 超出长度会截取  -1表示不处理
    // 中文字符串的长度应该小于 maxLen / 3
//    @Deprecated // 业务层判断并截取长度
//    int cutLen() default -1;

    // 是否需要对中文特殊字符编码
    boolean needEscape() default false;

    enum Strategy {
        UPPER,      // 大写
        LOWER,      // 小写
        CUSTOM,     // 自定义
        NONE        // 保持不变
    };

}
