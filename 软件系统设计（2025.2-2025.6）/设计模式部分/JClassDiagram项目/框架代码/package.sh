#!/bin/bash

# 检查是否存在必要的文件和目录
if [ ! -d "src/main" ] || [ ! -f "pom.xml" ]; then
    echo "错误：缺少 src/main 目录或 pom.xml 文件"
    exit 1
fi

# 打包成ZIP（自动递归目录）
zip -r project.zip src/main pom.xml

# 输出结果
echo "已生成压缩包：project.zip"
