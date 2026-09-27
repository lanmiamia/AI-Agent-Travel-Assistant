package com.yupi.yup_agent.demo.invoke;

import java.util.Arrays;

/**
 * @ Gareth Bale
 * @ version 1.0
 */
public class A {
    public static void main(String[] args) {
        int[] array = {15,9,8,7,6,5,4,3,2,1};
        System.out.println(Arrays.toString(array));
        A a = new A();
        a.sort(array,0,array.length-1);
        System.out.println(Arrays.toString(array));
    }

    public void sort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            sort(arr, low, pi - 1);   // 递归排序左半部分
            sort(arr, pi + 1, high);  // 递归排序右半部分
        }
    }

    // 分区：将数组分成小于基准和大于基准两部分，返回基准的最终位置
    public int partition(int[] arr, int low, int high) {
        int pivot = arr[high];  // 选最后一个元素为基准
        int i = low - 1;        // i 指向小于基准的最后一个位置

        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);  // 把基准放到正确位置
        return i + 1;
    }

    public void swap(int[] arr, int a , int b){
        int interval = arr[a];
        arr[a] = arr[b];
        arr[b] = interval;
    }
}
