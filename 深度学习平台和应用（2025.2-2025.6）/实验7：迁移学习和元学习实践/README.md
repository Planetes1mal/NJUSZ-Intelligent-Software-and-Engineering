# 实验七：迁移学习和元学习实践

## 实验任务

本次实验旨在通过Few-Shot分类任务理解迁移学习和元学习核心思想，共包含两个任务：**基于迁移学习的基线模型训练**（50分钟）和**MAML元学习模型实现**（100分钟）。

### 数据集说明

[下载训练集](https://box.nju.edu.cn/f/06579572abb542249517/?dl=1)

[下载测试集](https://box.nju.edu.cn/f/9508a9c341344d6e9164/?dl=1)

**Omniglot数据集**包含50种文字系统中的1623个手写字符，每个字符由20个不同书写者绘制。数据集划分为： - 训练/验证集：30种文字系统 - 每个文字系统中有多种文字 - 每个文字有20张图像![training_set](./assets/README/training_set.png)

- 测试集：640对支持集和查询集
- 5张支持图像
- 5张查询图像![testing_set](./assets/README/testing_set.png)

- 测试集标签说明：![data_example](./assets/README/support_query_set.png)

数据集下载代码将在提供的Jupyter Notebook中自动执行。



## 实验任务详解

### 任务一：迁移学习基线模型

迁移学习相关知识请参考[迁移学习和元学习介绍](https://zhiweinju.github.io/nju-dl-lab-2025spring/lab7/迁移学习和元学习介绍/#迁移学习)

[下载任务一和任务二代码](https://github.com/umnooob/course-demo/blob/main/docs/lab7/DL2025_lab7_Meta_Learning.ipynb)

#### 任务描述[¶](https://zhiweinju.github.io/nju-dl-lab-2025spring/lab7/实验七介绍/#_6)

1. **数据构造**：从训练集中随机选取5个Task
2. **模型构建**：一个简单的5层卷积神经网络，代码已在提供的Jupyter Notebook给出
3. **训练流程**：
4. 阶段一：在随机选取的5个Task数据上预训练分类模型
5. 阶段二：在5个支撑集上微调
6. 阶段三：在查询集推理

### 任务二：MAML元学习模型

元学习相关知识请参考[迁移学习和元学习介绍](https://zhiweinju.github.io/nju-dl-lab-2025spring/lab7/迁移学习和元学习介绍/#元学习)

#### 任务描述[¶](https://zhiweinju.github.io/nju-dl-lab-2025spring/lab7/实验七介绍/#_7)

1. **MAML核心思想**：
2. 在任务分布上学习一组**全局初始化参数**
3. 通过少量梯度更新快速适应新任务
4. **实现步骤**：
5. 采样任务批次（每批含32个Task）
6. 内循环（Inner Loop）：在每个Task的支撑集上执行梯度更新
7. 外循环（Outer Loop）：基于查询集损失更新元模型参数

#### 算法伪代码

![maml](./assets/README/maml.png)
（注：实际代码中将提供完整PyTorch实现)

------

## 实验资源

- [MAML原论文](https://arxiv.org/pdf/1703.03400.pdf)
- [PyTorch元学习库Torchmeta参考](https://github.com/tristandeleu/pytorch-meta)

------

## 实验提交

提交截止前需提供以下材料：

1. **代码文件**：
2. 完整可运行的Jupyter Notebook（含任务一、二实现）
3. **实验报告**（PDF格式）：

| 内容要求                                                     | 分值占比 |
| :----------------------------------------------------------- | :------- |
| 1. 绘制验证集准确率和任务数量的关系图                        | 20%      |
| 2. 一句话描述验证集准确率和任务数量的关系                    | 5%       |
| 3. 阅读[论文](https://arxiv.org/abs/1810.09502)并写出一个使用MAML时可能出现的问题 | 25%      |
| 4. 解释上一问的问题为什么会出现并给出你的解决方法            | 25%      |
| 5. 描述你对源代码做了哪些改进以提高测试准确率                | 25%      |

**性能指标**（基线模型 vs MAML在5-way 1-shot任务上的测试准确率）：

[下载测试集真实标签](https://github.com/umnooob/course-demo/blob/main/docs/lab7/true_labels.csv)

| 性能要求（可叠加） | 加分 |
| :----------------- | :--- |
| 基线模型acc>0.6    | +5分 |
| MAML acc>0.7       | +5分 |
| MAML acc>0.9       | +5分 |
| MAML acc>0.95      | +5分 |