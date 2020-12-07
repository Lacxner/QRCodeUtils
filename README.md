# 二维码工具类

基于Java的二维码编码和解码工具类，可以自定义二维码编码内容、二维码图片大小、Logo图片大小等，同时还可以对二维码图片进行解码。

需要导入Google的ZXing依赖：https://mvnrepository.com/artifact/com.google.zxing/core

## 方法

工具类中只包含最主要的两个方法，分别用于二维码的编码和解码，两个方法都提供了多个方法重载。

`encode`：对指定内容编码成二维码图片。

`decode`：对指定二维码图片进行解码。
