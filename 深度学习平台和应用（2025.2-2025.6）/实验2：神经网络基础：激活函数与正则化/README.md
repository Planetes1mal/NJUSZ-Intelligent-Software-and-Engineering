# 实验二：神经网络基础：激活函数与正则化

## 2.1 实验说明

### 开始之前

我们在本次实验中使用Jupyter Notebook进行实验，请参考[Jupyter Notebook使用](https://zhiweinju.github.io/nju-dl-lab-2025spring/lab1/环境配置指南/#jupyter-notebook)，按照文档中详细步骤进行操作，完成Jupyter Notebook的配置。

### 实验任务

本次实验分为两个任务：MINIST手写数字识别（约60分钟）和正则化和激活函数探索（约90分钟）。

## 2.2 任务一：MINIST手写数字识别

> [!NOTE]
>
> **实验目标**
>
> 通过本次实验，你将掌握以下内容：
>
> 1. 感知机、多层感知机(MLP)、激活函数的使用。
> 2. 实现一个多层感知机模型，用于MNIST数据集的数字分类。
> 3. 多种常用激活函数和正则化方法。

### 1. 感知机

> [!TIP]
>
> **感知机简介**
>
> 感知机是人工神经网络中最简单的一种形式，它由输入层、输出层组成，不包含隐藏层。

> [!WARNING]
>
> **感知机局限性**
>
> 感知机只能解决线性可分的问题，无法产生非线性分割面，因此无法解决非线性问题。

### 2. 多层感知机的从零实现

> [!TIP]
>
> **多层感知机介绍**
>
> 多层感知机是一种前馈人工神经网络模型，除了输入层和输出层之外还包含一个或多个隐藏层。每一层都完全连接到下一层。
> MLP可以学习非线性的函数映射，适用于更复杂的数据模式识别任务，如图像分类。

> [!NOTE]
>
> **MNIST数据集说明**
>
> MNIST数据集是由0〜9手写数字图片和数字标签所组成的，由60000个训练样本和10000个测试样本组成，
> 其中每个样本都是一张28 * 28像素的灰度手写数字图，适合初学者进行图像分类任务。

首先导入所需模块：

```python
import torch
import numpy as np
from torchvision import datasets, transforms
```

MNIST数据集是由0〜9手写数字图片和数字标签所组成的，由60000个训练样本和10000个测试样本组成，
其中每个样本都是一张28 * 28像素的灰度手写数字图，适合初学者进行图像分类任务。
加载与预处理MNIST数据集

```python
# 参数trainset应是一个实现了__len__和__getitem__方法的对象，代表整个MNIST训练数据集。
trainset = datasets.MNIST(root='./data', train=True, download=True, transform=transforms.ToTensor())
testset = datasets.MNIST(root='./data', train=False, download=True, transform=transforms.ToTensor())
```

创建数据加载器

```python
# batch_size=64表示每个批次包含64个样本。可以根据硬件（如内存/GPU显存）调整这个值。
# shuffle=True表示在每个epoch开始时都将训练数据集打乱顺序。
trainloader = torch.utils.data.DataLoader(trainset, batch_size=64, shuffle=True)
#对于测试集我们通常不需要打乱顺序
testloader = torch.utils.data.DataLoader(testset, batch_size=64, shuffle=False)
```

> [!TIP]
>
> **思考题**
>
> 如果不打乱训练集，会对训练结果产生什么影响？

定义模型所需参数

```python
num_inputs, num_outputs, num_hiddens = 784, 10, 256

W1 = torch.tensor(np.random.normal(0, 0.01, (num_inputs, num_hiddens)), dtype=torch.float)
b1 = torch.zeros(num_hiddens, dtype=torch.float)
W2 = torch.tensor(np.random.normal(0, 0.01, (num_hiddens, num_outputs)), dtype=torch.float)
b2 = torch.zeros(num_outputs, dtype=torch.float)

params = [W1, b1, W2, b2]
for param in params:
    param.requires_grad_(requires_grad=True)
```

定义激活函数，这里采用ReLU

```python
import matplotlib.pyplot as plt

# 定义ReLU函数
def relu(X):
    return torch.max(input=X, other=torch.tensor(0.0))

# 生成一系列X, Y值作为输入
x_values = torch.linspace(-5, 5, 100)
y_values = relu(x_values)

# 绘制图像
plt.figure(figsize=(8, 6)) # 设置图表大小
plt.plot(x_values.numpy(), y_values.numpy(), label='ReLU') # 绘制ReLU曲线
plt.title('ReLU Function') # 图表标题
plt.xlabel('Input Value') # x轴标签
plt.ylabel('Output Value') # y轴标签
plt.legend() # 显示图例
plt.grid(True) # 显示网格
plt.show() # 显示图表
```

定义模型结构

```python
def net(X): 
    X = X.view((-1, num_inputs)) # view函数将每张原始图像改成长度为num_inputs的向量
    H = relu(torch.matmul(X, W1) + b1)
    return torch.matmul(H, W2) + b2
```

定义损失函数，这里采用交叉熵损失

```python
loss = torch.nn.CrossEntropyLoss()
```

定义优化器，这里依然采用最简单的随机梯度下降方法

```python
def sgd(params, lr, batch_size):  
    """小批量随机梯度下降。"""
    with torch.no_grad():
        for param in params:
            # 对每个参数按照其梯度以及学习率进行更新。
            param -= lr * param.grad / batch_size
            # 我们希望根据新的数据重新计算梯度，而不是累加之前的梯度。
            param.grad.zero_()
```

定义准确率函数

```python
def evaluate_accuracy(dataloader, net):
    acc_sum, n = 0.0, 0
    for X, y in dataloader:
        #TODO
        #TODO
    return acc_sum / n
```

训练模型

```python
num_epochs = 5 
lr = 0.1

def train(net, train_iter, test_iter, loss, num_epochs, batch_size, params=None, lr=None, optimizer=None):
    for epoch in range(num_epochs):
        train_l_sum, train_acc_sum, n = 0.0, 0.0, 0
        for X, y in train_iter:
            y_hat = net(X)
            l = loss(y_hat, y).sum()

            # 梯度清零
            if optimizer is not None:
                optimizer.zero_grad()
            elif params is not None and params[0].grad is not None:
                for param in params:
                    param.grad.data.zero_()

            l.backward()
            if optimizer is None:
                sgd(params, lr, batch_size)  #梯度下降
            else:
                optimizer.step()  #更新参数

            # 计算准确率 TODO
            # 计算准确率 TODO
            n += y.shape[0]
        test_acc = evaluate_accuracy(test_iter, net)
        print('epoch %d, loss %.4f, train acc %.3f, test acc %.3f'
              % (epoch + 1, train_l_sum / n, train_acc_sum / n, test_acc))

train(net, trainloader, testloader, loss, num_epochs, batch_size=64, params=params, lr=lr)
```

### 3. 多层感知机的简单实现

首先加载MNIST数据集

```python
import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import datasets, transforms


trainset = datasets.MNIST(root='./data', train=True, download=True, transform=transforms.ToTensor())
testset = datasets.MNIST(root='./data', train=False, download=True, transform=transforms.ToTensor())
trainloader = torch.utils.data.DataLoader(trainset, batch_size=64, shuffle=True)
testloader = torch.utils.data.DataLoader(testset, batch_size=64, shuffle=False)
```

定义MLP模型

```python
num_inputs, num_outputs, num_hiddens = 784, 10, 256

class MLP(nn.Module):
    def __init__(self, num_inputs, num_outputs, num_hiddens):
        super(MLP, self).__init__()
        self.f1 = nn.Linear(num_inputs, num_hiddens) # 输入层 -> 隐藏层
        self.f2 = nn.Linear(num_hiddens, num_outputs) # 隐藏层 -> 输出层

    def forward(self, x):
        x = x.view(-1, num_inputs) # 将每张原始图像改成长度为num_inputs的向量
        x = F.relu(self.f1(x)) # 使用PyTorch中的ReLU激活函数
        x = self.f2(x)
        return x
```

实例化模型、损失函数和优化器

```python
net = MLP(num_inputs, num_outputs, num_hiddens)
loss = nn.CrossEntropyLoss()
optimizer = torch.optim.SGD(net.parameters(), lr=0.1)
```

定义准确率函数

```python
def evaluate_accuracy(dataloader, net):
    acc_sum, n = 0.0, 0
    for X, y in dataloader:
        #TODO
        #TODO
    return acc_sum / n
```

训练模型

```python
import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import datasets, transforms

trainset = datasets.MNIST(root='./data', train=True, download=True, transform=transforms.ToTensor())
testset = datasets.MNIST(root='./data', train=False, download=True, transform=transforms.ToTensor())
trainloader = torch.utils.data.DataLoader(trainset, batch_size=64, shuffle=True)
testloader = torch.utils.data.DataLoader(testset, batch_size=64, shuffle=False)

class MLP(nn.Module):
    def __init__(self, num_inputs, num_outputs, num_hiddens):
        super(MLP, self).__init__()
        self.f1 = nn.Linear(num_inputs, num_hiddens) # 输入层 -> 隐藏层
        self.f2 = nn.Linear(num_hiddens, num_outputs) # 隐藏层 -> 输出层

    def forward(self, x):
        x = x.view(-1, num_inputs) # 将每张原始图像改成长度为num_inputs的向量
        x = F.relu(self.f1(x)) # 使用PyTorch中的ReLU激活函数
        x = self.f2(x)
        return x


def train(net, train_iter, test_iter, loss, num_epochs, optimizer):
    for epoch in range(num_epochs):
        train_l_sum, train_acc_sum, n = 0.0, 0.0, 0
        for X, y in train_iter:
            y_hat = net(X)
            # 计算损失 TODO

            # 清除梯度 TODO

            # 反向传播 TODO
            # 更新参数 TODO

            # 计算准确率 TODO
            # 计算准确率 TODO
            n += y.shape[0]

        test_acc = evaluate_accuracy(test_iter, net)
        print('epoch %d, loss %.4f, train acc %.3f, test acc %.3f'
              % (epoch + 1, train_l_sum / n, train_acc_sum / n, test_acc))


num_epochs = 5
num_inputs, num_outputs, num_hiddens = 784, 10, 256
net = MLP(num_inputs, num_outputs, num_hiddens)
loss = nn.CrossEntropyLoss()
optimizer = torch.optim.SGD(net.parameters(), lr=0.1)

train(net, trainloader, testloader, loss, num_epochs, optimizer)
```

## 2.3 任务二：正则化和激活函数探索

### 正则化和激活函数探索

```python
import torch
import torch.nn as nn
import torch.optim as optim
import numpy as np
import matplotlib.pyplot as plt
from torch.utils.data import DataLoader
from torchvision import datasets, transforms

# 设置随机种子
torch.manual_seed(42)
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
```

```
device
```

### 激活函数实验

> [!NOTE]
>
> **激活函数概述**
>
> 激活函数是神经网络中的重要组成部分，它为网络引入非线性变换能力。本实验将探索三种常见的激活函数(ReLU、Sigmoid、Tanh)的特性及其对模型训练的影响。

#### 激活函数的实现与可视化

首先我们实现这三个激活函数及其导数，并通过可视化来理解它们的特性：

主要激活函数

- ReLU: $f(x)=max(0,x)$
- Sigmoid: $f(x)=1/(1+e^{−x})$
- Tanh: $f(x)=(e^x−e^{−x})/(e^x+e^{−x})$

```python
def check_activation_function(MyReLU,MySigmoid,MyTanh):
    x = torch.linspace(-10, 10, 1000)

    # 使用自己实现的激活函数
    my_relu = MyReLU()
    my_sigmoid = MySigmoid()
    my_tanh = MyTanh()

    # 计算激活函数值和导数值
    y_my_relu = my_relu(x)
    y_my_sigmoid = my_sigmoid(x)
    y_my_tanh = my_tanh(x)

    y_my_relu_derivative = my_relu.derivative(x)
    y_my_sigmoid_derivative = my_sigmoid.derivative(x)
    y_my_tanh_derivative = my_tanh.derivative(x)
    # 使用PyTorch的激活函数作为参考
    torch_relu = nn.ReLU()
    torch_sigmoid = nn.Sigmoid()
    torch_tanh = nn.Tanh()
    # 验证实现的正确性
    y_torch_relu = torch_relu(x)
    y_torch_sigmoid = torch_sigmoid(x)
    y_torch_tanh = torch_tanh(x)

    # 计算PyTorch激活函数的导数
    x_torch = x.clone().requires_grad_(True)  # 使x支持求导

    # ReLU导数
    y_torch_relu = torch_relu(x_torch)
    y_torch_relu.sum().backward()
    y_torch_relu_derivative = x_torch.grad.clone()
    x_torch.grad = None  # 清除梯度

    # Sigmoid导数
    y_torch_sigmoid = torch_sigmoid(x_torch)
    y_torch_sigmoid.sum().backward()
    y_torch_sigmoid_derivative = x_torch.grad.clone()
    x_torch.grad = None

    # Tanh导数
    y_torch_tanh = torch_tanh(x_torch)
    y_torch_tanh.sum().backward()
    y_torch_tanh_derivative = x_torch.grad.clone()

    assert torch.allclose(y_my_relu, y_torch_relu, rtol=1e-4, atol=1e-4), "ReLU实现有误"
    assert torch.allclose(y_my_sigmoid, y_torch_sigmoid, rtol=1e-4, atol=1e-4), "Sigmoid实现有误"
    assert torch.allclose(y_my_tanh, y_torch_tanh, rtol=1e-4, atol=1e-4), "Tanh实现有误"

    assert torch.allclose(y_my_relu_derivative, y_torch_relu_derivative, rtol=1e-4, atol=1e-4), "ReLU导数实现有误"
    assert torch.allclose(y_my_sigmoid_derivative, y_torch_sigmoid_derivative, rtol=1e-4, atol=1e-4), "Sigmoid导数实现有误"
    assert torch.allclose(y_my_tanh_derivative, y_torch_tanh_derivative, rtol=1e-4, atol=1e-4), "Tanh导数实现有误"

    # 绘制激活函数和导数
    plt.figure(figsize=(15, 6))

    # 绘制激活函数
    plt.subplot(1, 2, 1)
    plt.plot(x.numpy(), y_my_relu.numpy(), label='ReLU')
    plt.plot(x.numpy(), y_my_sigmoid.numpy(), label='Sigmoid')
    plt.plot(x.numpy(), y_my_tanh.numpy(), label='Tanh')
    plt.title('Activation Functions')
    plt.xlabel('x')
    plt.ylabel('y')
    plt.legend()
    plt.grid(True)

    # 绘制导数
    plt.subplot(1, 2, 2)
    plt.plot(x.numpy(), y_my_relu_derivative.numpy(), label='ReLU\'')
    plt.plot(x.numpy(), y_my_sigmoid_derivative.numpy(), label='Sigmoid\'')
    plt.plot(x.numpy(), y_my_tanh_derivative.numpy(), label='Tanh\'')
    plt.title('Derivatives of Activation Functions')
    plt.xlabel('x')
    plt.ylabel('y\'')
    plt.legend()
    plt.grid(True)

    plt.tight_layout()
    plt.show()
# 自己实现激活函数及其导数
class MyReLU:
    def __call__(self, x):
        """实现ReLU激活函数: f(x) = max(0, x)"""
        # TODO: 实现ReLU函数，返回x中所有元素与0的较大值
        # TODO: 可以使用torch.maximum函数实现ReLU

    def derivative(self, x):
        """ReLU的导数: f'(x) = 1 if x > 0 else 0"""
        # TODO: 实现ReLU的导数，当x>0时为1，否则为0
        # TODO: 可以使用torch.where函数实现ReLU的导数

class MySigmoid:
    def __call__(self, x):
        """实现Sigmoid激活函数: f(x) = 1 / (1 + e^(-x))"""
        # TODO: 实现Sigmoid函数，返回x中所有元素的Sigmoid值
        # TODO: 可以使用torch.exp函数实现e^(-x)

    def derivative(self, x):
        """Sigmoid的导数: f'(x) = f(x) * (1 - f(x))"""
       # TODO: 实现Sigmoid的导数，提示：可以利用__call__方法

class MyTanh:
    def __call__(self, x):
        """实现Tanh激活函数: f(x) = (e^x - e^(-x)) / (e^x + e^(-x))"""
        # TODO: 实现Tanh函数，返回x中所有元素的Tanh值
        # TODO: 可以使用torch.exp函数实现e^(-x)


    def derivative(self, x):
        """Tanh的导数: f'(x) = 1 - f(x)^2"""
        # TODO: 实现Tanh的导数，提示：可以利用__call__方法

# 测试实现的激活函数

check_activation_function(MyReLU,MySigmoid,MyTanh)
```

> [!TIP]
>
> **思考题**
>
> 思考题1：为什么神经网络需要非线性激活函数？如果使用线性激活函数会发生什么？

#### 梯度消失问题实验

梯度消失是深度神经网络训练中的一个常见问题。当网络层数较深时，在反向传播过程中梯度会随着层数的增加而逐渐减小，导致靠近输入层的参数几乎无法更新。本实验将：

1. 构建一个多层神经网络
2. 分别使用不同激活函数(ReLU、Sigmoid、Tanh)
3. 观察训练过程中各层的梯度分布
4. 分析不同激活函数对梯度消失问题的影响

```python
# 1. 梯度消失实验的网络
class Network(nn.Module):
    def __init__(self, input_dim, output_dim, activation_name, num_layers=5):
        super(Network, self).__init__()
        self.layers = nn.ModuleList()
        self.input_dim = input_dim

        # 创建很深的网络来测试梯度问题
        for _ in range(num_layers):
            self.layers.append(nn.Linear(100, 100))

        if activation_name == 'relu': 
            self.activation = MyReLU()
        elif activation_name == 'sigmoid':
            self.activation = MySigmoid()
        elif activation_name == 'tanh':
            self.activation = MyTanh()
        self.input_layer = nn.Linear(input_dim, 100)
        self.output_layer = nn.Linear(100, output_dim)

    def forward(self, x): 
        # TODO: 实现前向传播函数
        # 1. 首先通过输入层
        # 2. 应用激活函数
        # 3. 依次通过每个隐藏层并应用激活函数
        # 4. 最后通过输出层返回结果
def analyze_gradients(model):
    """分析模型各层的梯度分布"""
    gradients_by_layer = []
    for name, param in model.named_parameters():
        if param.grad is not None:
            # 计算每层梯度的统计信息
            layer_grads = param.grad.cpu().numpy()
            grad_mean = np.mean(np.abs(layer_grads))
            grad_std = np.std(layer_grads)
            gradients_by_layer.append({
                'layer': name,
                'mean': grad_mean,
                'std': grad_std
            })
    return gradients_by_layer
def get_data(batch_size=64):
    # 准备数据
    transform = transforms.Compose([
        transforms.ToTensor(),
        transforms.Normalize((0.1307,), (0.3081,))
    ])

    train_dataset = datasets.MNIST('./data', train=True, download=True, transform=transform)
    test_dataset = datasets.MNIST('./data', train=False, transform=transform)

    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
    test_loader = DataLoader(test_dataset, batch_size=batch_size, shuffle=False)
    return train_loader,test_loader

def plot_results(results):
    # 绘制结果
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 6))

    # 绘制准确率随epoch变化
    for activation in results:
        ax1.plot(results[activation]['acc_history'], label=f'{activation}')
    ax1.set_title('Training Accuracy over Epochs')
    ax1.set_xlabel('Epoch')
    ax1.set_ylabel('Accuracy (%)')
    ax1.legend()
    ax1.grid(True)

    # 绘制最后一个epoch的梯度分布
    for activation in results:
        last_grads = results[activation]['grad_history'][-1]
        ax2.semilogy(last_grads, label=f'{activation}')
    ax2.set_title('Gradient Distribution (Last Epoch)')
    ax2.set_xlabel('Layer Index')
    ax2.set_ylabel('Gradient Mean (log scale)')
    ax2.legend()
    ax2.grid(True)

    plt.tight_layout()
    plt.show()
def experiment_gradient_vanishing():
    """梯度消失实验"""

    criterion = nn.CrossEntropyLoss()
    # 训练不同激活函数的深层网络
    results = {activation: {'acc_history': [], 'grad_history': []} for activation in ['relu', 'sigmoid', 'tanh']}

    n_epochs = 5
    for activation in ['relu','sigmoid', 'tanh']:
        train_loader,_ = get_data()
        model = Network(input_dim=784, output_dim=10, activation_name=activation,num_layers=2).to(device)
        optimizer = optim.SGD(model.parameters(), lr=0.001)

        for epoch in range(n_epochs):
            model.train()
            total_loss = 0
            correct = 0
            total = 0

            # 训练阶段
            for batch_idx, (data, target) in enumerate(train_loader):
                data, target = data.to(device), target.to(device)
                data = data.view(data.size(0), -1)

                optimizer.zero_grad()
                output = model(data)
                loss = criterion(output, target)
                loss.backward()

                # 收集梯度信息
                if batch_idx % 1 == 0:
                    gradients = analyze_gradients(model)
                    results[activation]['grad_history'].append(
                        [g['mean'] for g in gradients]
                    )

                optimizer.step()

                # 计算准确率
                pred = output.argmax(dim=1, keepdim=True)
                correct += pred.eq(target.view_as(pred)).sum().item()
                total += target.size(0)
                total_loss += loss.item()

            # 计算epoch的平均准确率
            epoch_acc = 100. * correct / total
            results[activation]['acc_history'].append(epoch_acc)
            print(f'Activation: {activation}, Epoch: {epoch}, Accuracy: {epoch_acc:.2f}%')

    plot_results(results)

experiment_gradient_vanishing()
```

> [!TIP]
>
> **思考题**
>
> 思考题2：观察实验结果，为什么训练准确率会和激活函数选择相关？这与梯度分布有什么关系？
>
> 提示：
>
> - 比较不同激活函数的梯度范围
> - 分析梯度消失对模型训练的影响
> - 思考为什么ReLU在深度学习中更受欢迎

#### ReLU死亡现象实验

ReLU死亡现象指神经元在训练过程中持续输出0，导致参数无法更新的问题。本实验将：

1. 统计使用ReLU和Tanh激活函数时的神经元激活情况
2. 分析ReLU死亡现象的产生原因
3. 探讨如何缓解这一问题

```python
def experiment_relu_death():
    """ReLU死亡现象实验"""
    criterion = nn.CrossEntropyLoss()
    # 比较ReLU和LeakyReLU
    activation_counts = {'relu': [], 'tanh': []}

    for activation in ['relu', 'tanh']:
        model = Network(input_dim=784, output_dim=10, activation_name=activation, num_layers=2).to(device)
        optimizer = optim.SGD(model.parameters(), lr=0.001)

        # 训练几个epoch并记录激活值为0的神经元数量
        for epoch in range(5):
            zero_activations = 0
            total_activations = 0
            train_loader,_ = get_data()

            for data, target in train_loader:
                data = data.to(device)
                target = target.to(device)
                data = data.view(data.size(0), -1)
                optimizer.zero_grad()
                # 统计死亡神经元
                x = model.input_layer(data)
                x = model.activation(x)

                # 记录中间层的激活值
                for layer in model.layers:
                    x = layer(x)
                    activated = model.activation(x)

                    # 统计激活值为0的神经元数量
                    if activation in ['relu', 'tanh']:
                        zero_activations += torch.sum(activated == 0).item()
                        total_activations += activated.numel()

                # 继续前向传播完成训练
                output = model.output_layer(x)
                loss = criterion(output, target)
                loss.backward()
                optimizer.step()

            # 计算死亡率
            death_rate = zero_activations / total_activations
            print(f'Activation: {activation}, Epoch {epoch}: Death Rate: {death_rate:.2f}%')
            activation_counts[activation].append(death_rate)

    # 绘制死亡率随时间变化
    plt.figure(figsize=(10, 5))
    for activation, rates in activation_counts.items():
        plt.plot(rates, label=activation)
    plt.title('ReLU Death Rate During Training')
    plt.xlabel('Epoch')
    plt.ylabel('Death Rate')
    plt.legend()
    plt.show()

experiment_relu_death()
```

> [!TIP]
>
> **思考题**
>
> 思考题3：ReLU死亡现象的成因是什么？有哪些解决方案？
>
> 提示：
>
> - 分析什么情况下神经元会停止更新
> - 思考学习率、初始化方式的影响
> - 了解LeakyReLU等变体的优势

### 正则化方法实验

过拟合是机器学习中的常见问题，正则化是缓解过拟合的重要手段。本实验将探索两种主要的正则化方法：L2正则化和Dropout。

#### L2正则化实验

L2正则化通过在损失函数中添加权重的平方项来限制模型复杂度。本实验将：

1. 构造一个容易过拟合的数据集
2. 实现带有L2正则化的模型训练
3. 对比有无正则化的训练效果
4. 分析L2正则化对模型参数的影响

#### L2正则化

**1. 基本概念** L2正则化(也称为权重衰减)是深度学习中最常用的正则化技术之一。其核心思想是通过限制模型参数的大小来降低模型复杂度，从而防止过拟合。

**2. 数学表达**

在原始损失函数基础上添加L2正则项：
$$
L_{total}(\mathbf{w},b)=L_{original}(\mathbf{w},b)+\frac{\lambda}{2}\|\mathbf{w}\|^2
$$
其中：

- $L_{original}$ 是原始损失函数(如均方误差)
- $\|\mathbf{w}\|^2$ 是权重向量的L2范数平方
- $\lambda$ 是正则化系数，控制正则化的强度

**3. 工作原理**

1. **参数惩罚**：
   - L2正则化通过惩罚较大的权重参数，鼓励模型学习更小的权重值
   - 较大的权重往往意味着模型对输入特征的依赖程度更高，更容易过拟合
2. **平滑效果**：
   - L2正则化倾向于将权重均匀分散到所有特征上
   - 这使得模型不会过分依赖某些特定特征，提高了泛化能力
3. **梯度更新**：
   - 在参数更新时，L2正则化项的梯度为 λw
   - 这相当于在每次更新时将权重缩小一个比例，故称为权重衰减

**4. 与L1正则化的对比**

- L2正则化倾向于产生值较小但非零的权重，权重呈现正态分布
- L1正则化倾向于产生稀疏的权重向量，即许多权重为零
- L2正则化在特征之间有关联时更适用，而L1正则化更适合特征选择

**5. 实践应用**

- 正则化系数 λ 是一个需要调整的超参数
- λ=0 时相当于无正则化
- λ 越大，正则化效果越强，模型越简单，但可能欠拟合
- 通常通过交叉验证来选择合适的 λ 值

##### 数据集定义

给定x，我们将**使用以下三阶多项式来生成训练和测试数据的标签：**
$$
y=0.05+\sum_{i=1}^d0.01x_i+\epsilon,\mathrm{~where~}\epsilon\sim\mathcal{N}(0,0.01^2).
$$
我们选择标签是关于输入的线性函数。 标签同时被均值为0，标准差为0.01高斯噪声破坏。 为了使过拟合的效果更加明显，我们可以将问题的维数增加到d=200， 并使用一个只包含20个样本的小训练集。

```python
# 设置维度和样本数
d = 200  # 特征维度
n_train, n_test = 20, 100  # 训练和测试数据集大小

# 生成特征数据
features = np.random.normal(size=(n_train + n_test, d))

# 生成标签
# y = 0.05 + 0.01 * sum(x_i) + epsilon
labels = 0.05 + 0.01 * np.sum(features, axis=1)
# 添加噪声 epsilon ~ N(0, 0.01^2)
labels += np.random.normal(0, 0.01, size=labels.shape)

# 转换为tensor
features = torch.tensor(features, dtype=torch.float32)
labels = torch.tensor(labels, dtype=torch.float32)

# 分割训练集和测试集
train_features = features[:n_train]
test_features = features[n_train:]
train_labels = labels[:n_train]
test_labels = labels[n_train:]
```

##### 定义L2范数惩罚

实现这一惩罚最方便的方法是对所有项求平方后并将它们求和。

```python
def l2_penalty(w):
    # TODO: 使用torch.sum和.pow方法实现L2范数惩罚
    # 提示: 对权重参数w平方求和，再除以2
```

##### 对模型进行训练和测试

让我们**实现一个函数来评估模型在给定数据集上的损失**，及**训练函数**。

```python
class Accumulator:
    """在n个变量上累加"""
    def __init__(self, n):
        self.data = [0.0] * n

    def add(self, *args):
        self.data = [a + float(b) for a, b in zip(self.data, args)]

    def reset(self):
        self.data = [0.0] * len(self.data)

    def __getitem__(self, idx):
        return self.data[idx]

def evaluate_loss(net, data_iter, loss):
    """评估给定数据集上模型的损失"""
    metric = Accumulator(2)  # 损失的总和,样本数量
    for X, y in data_iter:
        out = net(X)
        y = y.reshape(out.shape)
        l = loss(out, y)
        metric.add(l.sum(), l.numel())
    return metric[0] / metric[1]

def train_epoch(net, train_iter, loss, trainer, penalty_lambda):
    """训练模型一个epoch"""
    # 将模型设置为训练模式
    net.train()
    # 训练损失总和、训练样本数、实例数
    metric = Accumulator(3)
    for X, y in train_iter:
        trainer.zero_grad()
        y_hat = net(X)
        weight = net[0].weight
        l = loss(y_hat, y.reshape(y_hat.shape)) + penalty_lambda * l2_penalty(weight)
        l.mean().backward()
        trainer.step()
        metric.add(float(l.sum()), y.numel(), 1)
    # 返回训练损失和训练准确率
    return metric[0] / metric[2]

def train(train_features, test_features, train_labels, test_labels,
          num_epochs=100, penalty_lambda=0):
    loss = nn.MSELoss(reduction='none')
    input_shape = train_features.shape[-1]
    # 不设置偏置，因为我们已经在多项式中实现了它
    net = nn.Sequential(nn.Linear(input_shape, 1, bias=False))
    batch_size = min(10, train_labels.shape[0])

    # 创建数据迭代器
    train_iter = DataLoader(
        torch.utils.data.TensorDataset(train_features, train_labels.reshape(-1,1)),
        batch_size=batch_size)
    test_iter = DataLoader(
        torch.utils.data.TensorDataset(test_features, test_labels.reshape(-1,1)),
        batch_size=batch_size)

    trainer = torch.optim.SGD(net.parameters(), lr=0.003)

    # 记录训练过程
    train_loss = []
    test_loss = []

    for epoch in range(num_epochs):
        train_epoch(net, train_iter, loss, trainer, penalty_lambda)
        if epoch == 0 or (epoch + 1) % 20 == 0:
            train_l = evaluate_loss(net, train_iter, loss)
            test_l = evaluate_loss(net, test_iter, loss)
            train_loss.append(train_l)
            test_loss.append(test_l)
            print(f'epoch {epoch+1}, train loss {train_l:.3f}, test loss {test_l:.3f}')

    # 绘制损失曲线
    plt.figure(figsize=(10, 6))
    plt.semilogy(range(1, len(train_loss) * 20 + 1, 20), train_loss, label='train')
    plt.semilogy(range(1, len(test_loss) * 20 + 1, 20), test_loss, label='test')
    plt.xlabel('epoch')
    plt.ylabel('loss')
    plt.legend()
    plt.grid(True)
    plt.show()

    print('weight:', net[0].weight.data.numpy())
# 不使用正则化
train(train_features, test_features,
      train_labels, test_labels)
# 使用L2正则化
train(train_features, test_features,
      train_labels, test_labels, penalty_lambda=3)
```

##### 简洁实现

在深度学习框架中，我们无需实现L2正则化，只需要在损失函数中添加正则项。

```python
def train_epoch(net, train_iter, loss, trainer):
    """训练模型一个epoch"""
    # 将模型设置为训练模式
    net.train()
    # 训练损失总和、训练样本数、实例数
    metric = Accumulator(3)
    for X, y in train_iter:
        trainer.zero_grad()
        y_hat = net(X)
        weight = net[0].weight
        l = loss(y_hat, y.reshape(y_hat.shape))
        l.mean().backward()
        trainer.step()
        metric.add(float(l.sum()), y.numel(), 1)
    # 返回训练损失和训练准确率
    return metric[0] / metric[2]

def train(train_features, test_features, train_labels, test_labels,
          num_epochs=100, penalty_lambda=0):
    loss = nn.MSELoss(reduction='none')
    input_shape = train_features.shape[-1]
    # 不设置偏置，因为我们已经在多项式中实现了它
    net = nn.Sequential(nn.Linear(input_shape, 1, bias=False))
    batch_size = min(10, train_labels.shape[0])

    # 创建数据迭代器
    train_iter = DataLoader(
        torch.utils.data.TensorDataset(train_features, train_labels.reshape(-1,1)),
        batch_size=batch_size)
    test_iter = DataLoader(
        torch.utils.data.TensorDataset(test_features, test_labels.reshape(-1,1)),
        batch_size=batch_size)

    trainer = torch.optim.SGD(net.parameters(), lr=0.003, weight_decay=penalty_lambda)

    # 记录训练过程
    train_loss = []
    test_loss = []

    for epoch in range(num_epochs):
        train_epoch(net, train_iter, loss, trainer)
        if epoch == 0 or (epoch + 1) % 20 == 0:
            train_l = evaluate_loss(net, train_iter, loss)
            test_l = evaluate_loss(net, test_iter, loss)
            train_loss.append(train_l)
            test_loss.append(test_l)
            print(f'epoch {epoch+1}, train loss {train_l:.3f}, test loss {test_l:.3f}')

    # 绘制损失曲线
    plt.figure(figsize=(10, 6))
    plt.semilogy(range(1, len(train_loss) * 20 + 1, 20), train_loss, label='train')
    plt.semilogy(range(1, len(test_loss) * 20 + 1, 20), test_loss, label='test')
    plt.xlabel('epoch')
    plt.ylabel('loss')
    plt.legend()
    plt.grid(True)
    plt.show()

    print('weight:', net[0].weight.data.numpy())
# 使用L2正则化
train(train_features, test_features,
      train_labels, test_labels, penalty_lambda=3)
```

> [!TIP]
>
> **思考题**
>
> 思考题4：使用L2正则化后，模型的参数会发生什么变化？为什么这种变化有助于防止过拟合？
>
> 提示：
>
> - 观察权重的数值分布变化
> - 分析正则化系数λ的影响
> - 思考为什么较小的权重有助于泛化

#### Dropout

> [!NOTE]
>
> **Dropout原理**
>
> *Dropout* 是一种在深度学习中广泛使用的正则化技术。 与L2正则化通过限制权重大小来防止过拟合不同， Dropout通过在训练过程中随机"丢弃"（设置为零）神经元来实现正则化。

> [!NOTE]
>
> **Dropout数学表达**
>
> 在训练过程中，对于每个样本，每一层的每个神经元都有概率 p 被暂时从网络中移除。 具体来说，如果一个神经元的输出为 h，则：
> $$
> \begin{cases}\frac{h}{1-p}&\text{概率 }1-p\left(\text{保留}\right)\\0&\text{概率 }p\left(\text{丢弃}\right)\end{cases}
> $$
> 这里的 $\frac{h}{1-p}$ 是一个缩放因子，用于保持输出的期望值不变。

> [!IMPORTANT]
>
> **Dropout优点**
>
> 1. **防止神经元的共适应性（Co-adaptation）**
>    - 因为神经元不能依赖于特定的其他神经元的存在
>    - 必须学会与随机的神经元子集一起工作
> 2. **提供了一种廉价的模型集成方法**
>    - 每次使用Dropout相当于训练一个新的网络架构
>    - 最终模型可以看作是多个子网络的集成
> 3. **减少神经元之间的复杂共适应关系**
>    - 每个神经元必须学会更鲁棒的特征
>    - 不能过分依赖某些特定的特征组合

> [!NOTE]
>
> **实践建议**
>
> 在实践中，Dropout通常在全连接层中使用， 丢弃概率 p 通常设置为0.5， 而在卷积层中较少使用或使用较小的丢弃概率（如0.1）。

```python
def dropout_layer(X, dropout):
    assert 0 <= dropout <= 1
    device = X.device
    # TODO: 实现dropout层
    # 1. 如果dropout=1，返回全0张量
    # 2. 如果dropout=0，直接返回输入X
    # 3. 否则，生成一个与X形状相同的随机掩码(mask)
    #    - 使用torch.rand生成随机数，并与dropout比较创建二元掩码
    #    - 将X与掩码相乘，并除以(1-dropout)进行缩放
    # 注意：过程中device参数需要与X的设备相同
    # 在本情况中，所有元素都被丢弃
    if dropout == 1:
        return torch.zeros_like(X, device=device)
    # 在本情况中，所有元素都被保留
    if dropout == 0:
        # TODO: 实现dropout=0的情况
    # TODO: 实现dropout=其他值的情况
    mask = 
    return mask * X / (1.0 - dropout)
```

我们可以通过下面几个例子来**测试`dropout_layer`函数**。 我们将输入`X`通过dropout操作，dropout概率分别为0、0.5和1。

```python
X= torch.arange(16, dtype = torch.float32).reshape((2, 8))
print(X)
print(dropout_layer(X, 0.))
print(dropout_layer(X, 0.5))
print(dropout_layer(X, 1.))
```

##### 定义模型

我们可以将dropout应用于每个隐藏层的输出（在激活函数之后），并且dropout只在训练期间有效。

```python
class RegularizedNetwork(nn.Module):
    def __init__(self, dropout_rate=0.0):
        super(RegularizedNetwork, self).__init__()
        self.dropout_rate = dropout_rate
        self.training = True
        self.conv1 = nn.Conv2d(1, 32, 3, 1)
        self.conv2 = nn.Conv2d(32, 64, 3, 1)
        self.fc1 = nn.Linear(1600, 128)
        self.fc2 = nn.Linear(128, 10)

    def forward(self, x):
        x = torch.relu(self.conv1(x))
        x = torch.max_pool2d(x, 2)
        x = torch.relu(self.conv2(x))
        x = torch.max_pool2d(x, 2)
        x = torch.flatten(x, 1)
        if self.training:
            x = dropout_layer(x, self.dropout_rate)
        x = torch.relu(self.fc1(x))
        x = self.fc2(x)
        return x
def experiment_regularization():
    """比较不同正则化方法的效果"""

    # 实验配置
    configs = {
        'No Regularization': {'dropout': 0.0},
        'Dropout': {'dropout': 0.3},
    }

    results = {name: {'train_acc': [], 'test_acc': []} for name in configs.keys()}
    n_epochs = 15

    for name, config in configs.items():
        print(f"\nTraining with {name}")
        model = RegularizedNetwork(dropout_rate=config['dropout']).to(device)

        optimizer = optim.SGD(model.parameters(),lr=0.5)
        criterion = nn.CrossEntropyLoss()
        train_loader,test_loader = get_data(batch_size=256)
        for epoch in range(n_epochs):
            # 训练阶段
            model.train()
            train_correct = 0
            train_total = 0
            for data, target in train_loader:
                data, target = data.to(device), target.to(device)
                optimizer.zero_grad()
                output = model(data)
                loss = criterion(output, target)
                loss.backward()
                optimizer.step()

                pred = output.argmax(dim=1, keepdim=True)
                train_correct += pred.eq(target.view_as(pred)).sum().item()
                train_total += target.size(0)

            train_acc = 100. * train_correct / train_total

            # 测试阶段
            model.eval()
            test_correct = 0
            test_total = 0

            with torch.no_grad():
                for data, target in test_loader:
                    data, target = data.to(device), target.to(device)
                    output = model(data)
                    pred = output.argmax(dim=1, keepdim=True)
                    test_correct += pred.eq(target.view_as(pred)).sum().item()
                    test_total += target.size(0)

            test_acc = 100. * test_correct / test_total

            # 记录结果
            results[name]['train_acc'].append(train_acc)
            results[name]['test_acc'].append(test_acc)

            print(f'Epoch {epoch}: Train Acc: {train_acc:.2f}%, Test Acc: {test_acc:.2f}%')

    # 绘制结果
    plt.figure(figsize=(15, 5))

    # 训练准确率
    plt.subplot(1, 2, 1)
    for name in results:
        plt.plot(results[name]['train_acc'], label=name)
    plt.title('Training Accuracy')
    plt.xlabel('Epoch')
    plt.ylabel('Accuracy (%)')
    plt.legend()
    plt.grid(True)
    # 测试准确率
    plt.subplot(1, 2, 2)
    for name in results:
        plt.plot(results[name]['test_acc'], label=name)
    plt.title('Test Accuracy')
    plt.xlabel('Epoch')
    plt.ylabel('Accuracy (%)')
    plt.legend()
    plt.grid(True)

    plt.tight_layout()
    plt.show()

# 运行实验
experiment_regularization()
```

##### Pytorch简洁实现

dropout可以作为`nn.Module`类的一个模块来使用，

```python
nn.Dropout(dropout_rate)
class RegularizedNetwork(nn.Module):
    def __init__(self, dropout_rate=0.0):
        super(RegularizedNetwork, self).__init__()
        self.conv1 = nn.Conv2d(1, 32, 3, 1)
        self.conv2 = nn.Conv2d(32, 64, 3, 1)
        self.dropout = nn.Dropout(dropout_rate) if dropout_rate>0 else None
        self.fc1 = nn.Linear(1600, 128)
        self.fc2 = nn.Linear(128, 10)

    def forward(self, x):
        x = torch.relu(self.conv1(x))
        x = torch.max_pool2d(x, 2)
        x = torch.relu(self.conv2(x))
        x = torch.max_pool2d(x, 2)
        x = torch.flatten(x, 1)
        if self.dropout:
            x = self.dropout(x)
        x = torch.relu(self.fc1(x))
        x = self.fc2(x)
        return x

experiment_regularization()
```

> [!TIP]
>
> **思考题**
>
> 思考题5：Dropout为什么能够起到正则化的作用？训练时和测试时的差异处理有什么意义？
>
> 提示：
>
> - 分析Dropout的集成学习观点
> - 思考为什么要进行比例缩放
> - 考虑Dropout对特征依赖的影响