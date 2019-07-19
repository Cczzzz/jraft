package com.jraft.kit;

import java.util.function.Function;

/**
 * Created by chenchang on 2018/10/10.
 * 避免繁琐null判断 和三目运算符
 */
public class AvertNull<T> {

    private T value;

    public AvertNull(T value) {
        this.value = value;
    }

    public static <T> AvertNull<T> of(T t) {
        return new AvertNull<>(t);
    }

    //如果对象为空 执行or
    //如果对象不为空 执行函数，如果函数返回null 执行 or
    public <R> avert<T, R> get(Function<T, R> function) {
        return new avert<>(value, function);
    }

    //如果对象为空 返回空
    //如果对象不为空 执行函数
    public <R> R getOrNull(Function<T, R> function) {
        if (value == null) {
            return null;
        }
        return function.apply(value);
    }

    public static class avert<T, R> {
        private T value;

        private Function<T, R> function;

        private avert(T value, Function<T, R> function) {
            this.value = value;
            this.function = function;
        }

        public R or(R r) {
            if (value != null) {
                return function.apply(value);
            } else {
                return r;
            }
        }
    }
    static class Bean {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    //example
    public static void main(String[] args) {
        Bean bean = null;
        //println  这个对象为空
        System.out.println(AvertNull.of(bean).get(Bean::getName).or("这个对象为空"));
        //println  null
        System.out.println(AvertNull.of(bean).getOrNull(Bean::getName));


        Bean bean2 = new Bean();
        bean2.setName("name");
        //println  name
        System.out.println(AvertNull.of(bean2).get(Bean::getName).or("这个对象为空"));
        //println  name
        System.out.println(AvertNull.of(bean2).getOrNull(Bean::getName));
    }




}
