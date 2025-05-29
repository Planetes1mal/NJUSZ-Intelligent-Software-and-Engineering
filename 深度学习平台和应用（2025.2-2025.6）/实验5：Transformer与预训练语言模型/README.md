# 实验五：Transformer与预训练语言模型

## 实验任务一：Transformer

### 1. Transformer 编码器中 Encoder Layer 的实现

> **Encoder Layer简介**
>
> Encoder Layer 是 Transformer 编码器中的基本构建单元，由 多头自注意力机制（Multi-Head Self-Attention） 和 前馈全连接网络（Feed Forward Network） 组成，搭配两次残差连接与 LayerNorm，用于高效建模输入序列的上下文依赖关系和特征表达能力。
>

这次我们还是使用AG News 数据集进行后续的分类任务，由于读取数据方式的改变，需要重新下载一下数据集。

ag数据下载链接：https://box.nju.edu.cn/f/a3a2e11167ef4d72a568/?dl=1

预训练模型（BERT）下载链接：https://box.nju.edu.cn/d/2710380144234ce78fe3/

可能需要安装transformers包

```
pip install transformers
```

#### 预处理

首先导入所需模块：

```python
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
from torch.nn.utils.rnn import pad_sequence
# from torchtext.data.utils import get_tokenizer
# from torchtext.vocab import build_vocab_from_iterator
from tqdm import tqdm
import pandas as pd
from transformers import AutoTokenizer
```

数据读取以及预处理

不同于上次，这次我们使用pandas读取数据，相应的代码也有所修改。

```python
# **1. 加载 AG NEWS 数据集**
df = pd.read_csv("train.csv")  # 请替换成你的文件路径
df.columns = ["label", "title", "description"]  # CSV 有3列：标签, 标题, 描述
df["text"] = df["title"] + " " + df["description"]  # 合并标题和描述作为输入文本
df["label"] = df["label"] - 1  # AG NEWS 的标签是 1-4，我们转换成 0-3
train_texts, train_labels = df["text"].tolist(), df["label"].tolist()
number = int(0.3 * len(train_texts))
train_texts, train_labels = train_texts[：number], train_labels[：number]

df = pd.read_csv("test.csv")  # 请替换成你的文件路径
df.columns = ["label", "title", "description"]  # CSV 有3列：标签, 标题, 描述
df["text"] = df["title"] + " " + df["description"]  # 合并标题和描述作为输入文本
df["label"] = df["label"] - 1  # AG NEWS 的标签是 1-4，我们转换成 0-3
test_texts, test_labels = df["text"].tolist(), df["label"].tolist()

# **2. 加载 BERT Tokenizer**
model_name = "bert-base-uncased"
tokenizer = AutoTokenizer.from_pretrained(model_name)

# **3. 处理数据**
class AGNewsDataset(Dataset):
    def __init__(self, texts, labels, tokenizer, max_length=50):
        self.texts = texts
        self.labels = labels
        self.tokenizer = tokenizer
        self.max_length = max_length

    def __len__(self):
        return len(self.texts)

    def __getitem__(self, idx):
        text = self.texts[idx]
        label = self.labels[idx]
        encoding = self.tokenizer(
            text, truncation=True, padding="max_length", max_length=self.max_length, return_tensors="pt"
        )
        input_ids = encoding["input_ids"].squeeze(0)
        return input_ids, torch.tensor(label, dtype=torch.long)

vocab = tokenizer.get_vocab()
pad_idx = tokenizer.pad_token_id
unk_idx = tokenizer.unk_token_id

train_dataset = AGNewsDataset(train_texts, train_labels, tokenizer)
test_dataset = AGNewsDataset(test_texts, test_labels, tokenizer)

train_dataloader = DataLoader(train_dataset, batch_size=16, shuffle=True)
test_dataloader = DataLoader(test_dataset, batch_size=16, shuffle=False)
```

#### 位置编码器

在 Transformer 模型中，Self-Attention 是无序的，它无法感知输入序列的「位置信息」，即每个 token 在序列中的先后顺序。

相比之下，RNN（通过时间步）和 CNN（通过局部感受野）天然就有顺序/位置的概念。

因此，Transformer 需要为每个 token embedding 加入「位置信息」，这就是 Positional Encoding 的作用。

论文《Attention is All You Need》中提出了如下的位置编码方式，使用正弦和余弦函数来构造具有不同频率的位置表示。

原始公式：

$$
PE(pos,2i)=\sin\left(\frac{pos}{10000^{\frac{2i}{d_{model}}}}\right)
\\
PE(pos,2i+1)=\cos\left(\frac{pos}{10000^{\frac{2i}{d_{model}}}}\right)
$$
其中：

- $pos$：是序列中的位置 ($0, 1, 2, \ldots, L - 1$)
- $i$：是 embedding 的维度索引 ($0  d_{model}/2$)
- $d_{model}$：是 embedding 的总维度
- $10000$：是一个控制不同维度的 $sin/cos$ 波动频率的超参数

公式进一步推导， 原公式中：

$$
\frac{pos}{10000^{\frac{2i}{d_{model}}}}
$$
等价于：

$$
pos \times \frac{1}{10000^{\frac{2i}{d_{model}}}}
$$
进一步展开成指数形式：

$$
= pos \times e^{-\log(10000) \cdot \frac{2i}{d_{model}}}
$$
请参考展开后的指数形式补充完下面的代码：

```python
#这段代码是 Transformer中的位置编码（PositionalEncoding），用于给输入的 token embedding 加入位置信息。
class PositionalEncoding(nn.Module):
    def __init__(self, d_model, max_len=5000):
        super().__init__()

        # 创建一个全0的矩阵，shape = (max_len, d_model)
        # 表示每个位置 (0 ~ max_len-1) 对应的 d_model 维位置编码
        pe = torch.zeros(max_len, d_model)

        # 生成位置索引，shape = (max_len, 1)
        # 即 position = [0, 1, 2, ..., max_len-1] 的列向量
        position = torch.arange(0, max_len).unsqueeze(1)

        # TODO 1：计算 div_term，用于控制不同维度的 sin/cos 频率
        # 要求：使用 torch.exp() 实现 1 / 10000^(2i/d_model)
        div_term = ...

        # TODO 2：给偶数维度位置编码赋值
        # 要求：使用 torch.sin() 完成 position * div_term，赋值给 pe 的偶数列
        pe[:, 0::2] = ...

        # TODO 3：给奇数维度位置编码赋值
        # 要求：使用 torch.cos() 完成 position * div_term，赋值给 pe 的奇数列
        pe[:, 1::2] = ...

        # 将 pe 注册为 buffer（不会被训练优化）
        # 并扩展成 (1, max_len, d_model) 方便后续和 batch 做广播
        self.register_buffer('pe', pe.unsqueeze(0))  # shape：(1, max_len, d_model)

    def forward(self, x):
        # x 是输入的 embedding，shape = (batch_size, seq_len, d_model)

        # 将对应位置的 pe 加到 x 上
        # self.pe[:, :x.size(1)] shape = (1, seq_len, d_model) 自动广播到 batch_size
        x = x + self.pe[:, :x.size(1)]

        # 返回位置编码后的 embedding
        return x
```

> [!TIP]
>
> **思考题**
>
> 思考题1：为什么需要对偶数和奇数维度分别使用 sin 和 cos？
>

#### Multi-Head Self-Attention 模块

下面是多头自注意力的实现，请你按照要求补全代码：

```python
# Multi-Head Self-Attention 的完整实现
class MultiHeadSelfAttention(nn.Module):
    def __init__(self, d_model, n_heads):
        super().__init__()
        # 保证 d_model 可以被 n_heads 整除，方便分头
        assert d_model % n_heads == 0
        self.d_k = d_model // n_heads # 每个 head 的特征维度
        self.n_heads = n_heads

        # 共享一个 Linear 层同时生成 Q, K, V
        self.qkv_linear = nn.Linear(d_model, d_model * 3) # 输出为 [Q; K; V]

        # 输出层，将多头的结果重新映射回 d_model 维度
        self.fc = nn.Linear(d_model, d_model)

    def forward(self, x, mask=None):
        # 输入 x：(batch_size, seq_len, d_model)
        batch_size, seq_len, d_model = x.size()

        # 一次性计算 Q、K、V，输出 shape = (batch_size, seq_len, 3 * d_model)
        qkv = self.qkv_linear(x)

        # 切分成 n_heads 个 head，准备 multi-head attention
        # shape 变为 (batch_size, seq_len, n_heads, 3 * d_k)
        qkv = qkv.view(batch_size, seq_len, self.n_heads, 3 * self.d_k)

        # 调整维度顺序，变成 (batch_size, n_heads, seq_len, 3 * d_k)
        qkv = qkv.permute(0, 2, 1, 3)  # (batch_size, n_heads, seq_len, 3*d_k)

        # 沿最后一个维度切成 Q, K, V，shape = (batch_size, n_heads, seq_len, d_k)
        q, k, v = qkv.chunk(3, dim=-1)  # (batch_size, n_heads, seq_len, d_k)

        # TODO 1：计算 attention scores
        # 要求：使用缩放点积的方式计算 (Q x K^T)，并除以 sqrt(d_k)
        scores = ...

        # mask 操作，屏蔽掉 padding 部分
        if mask is not None:
            scores = scores.masked_fill(mask == 0, -1e9)

        # TODO 2：计算 attention 权重
        # 要求：在 seq_len 维度上使用 softmax 归一化 scores
        attn = ...

        # TODO 3：计算加权求和后的 context
        # 要求：用 attn 加权 V，得到 context
        context = ...

        # 将多头拼接回去，shape = (batch_size, seq_len, n_heads * d_k) = (batch_size, seq_len, d_model)
        context = context.transpose(1, 2).contiguous().view(batch_size, seq_len, d_model)

        # 通过输出层，再映射回原始 d_model 维度
        return self.fc(context)
```

> [!TIP]
>
> **思考题**
>
> 思考题2：在 Multi-Head Self-Attention 机制中，为什么我们需要使用多个 attention head？
>
> 思考题3：为什么要用缩放因子 sqrt(d_k)？
>

#### TransformerEncoderLayer

下面的代码实现了 Transformer 编码器中的一个标准 Encoder Layer，包含：

“多头自注意力 + 前馈网络 + 两次残差连接 + 两次 LayerNorm” 的结构，用于对输入序列进行特征建模和上下文信息融合。

请你按照要求补全代码：

```python
class TransformerEncoderLayer(nn.Module):
    def __init__(self, d_model, n_heads, d_ff):
        super().__init__()

        # 多头自注意力模块，输入输出维度都是 d_model
        self.self_attn = MultiHeadSelfAttention(d_model, n_heads)

        # 前馈全连接层，包含两层线性 + ReLU
        self.ff = nn.Sequential(
            nn.Linear(d_model, d_ff),
            nn.ReLU(),
            nn.Linear(d_ff, d_model)
        )

        # 第一层 LayerNorm，作用在自注意力的残差连接之后
        self.norm1 = nn.LayerNorm(d_model)
        # 第二层 LayerNorm，作用在前馈网络的残差连接之后
        self.norm2 = nn.LayerNorm(d_model)

    def forward(self, x, mask=None):
        # ------------------ 自注意力块 ------------------ #

        # TODO 1：计算多头自注意力输出 x2
        x2 = ...

        # TODO 2：残差连接 + 第一层 LayerNorm
        x = ...

        # ------------------ 前馈神经网络块 ------------------ #

        # TODO 3：前馈全连接网络（两层 Linear + ReLU）得到 x2
        x2 = ...

        # TODO 4：残差连接 + 第二层 LayerNorm
        x = ...


        return x
```

> [!TIP]
>
> **思考题**
>
> 思考题4：为什么 Transformer Encoder Layer 中要在 Self-Attention 和 Feed Forward Network 之后都使用残差连接和 LayerNorm？试从“模型训练稳定性”和“特征传递”两个角度进行分析。
>

### 2. 基于 Transformer Encoder 的文本分类器

下面，我们实现一个基于 Transformer Encoder 的文本分类器，通过 embedding、位置编码、多层 encoder 处理输入序列，最终使用 mean pooling 和全连接层完成文本的多类别分类任务。

```python
class TransformerEncoderClassifier(nn.Module):
    def __init__(self, vocab_size, d_model=128, n_heads=4, d_ff=256, num_layers=2, num_classes=4):
        super().__init__()

        # 1. 定义词嵌入层（Embedding），输入为词表大小，输出为 d_model 维
        # padding_idx 用于指定 padding token 的索引，避免其被训练
        self.embedding = nn.Embedding(vocab_size, d_model, padding_idx=pad_idx)

        # 2. 定义位置编码器，为 token embedding 添加位置信息
        self.pos_encoder = PositionalEncoding(d_model)

        # 3. 定义多个 TransformerEncoderLayer 叠加起来，num_layers 为层数
        self.layers = nn.ModuleList([TransformerEncoderLayer(d_model, n_heads, d_ff) for _ in range(num_layers)])

        # 4. 定义输出分类层，将 encoder 最终输出映射到 num_classes 维度
        self.fc = nn.Linear(d_model, num_classes)

    def forward(self, x):
        # x shape：(batch_size, seq_len)，输入为单词 ID 序列

        # 1. 输入 token ID 通过 Embedding，转成 (batch_size, seq_len, d_model) 的 dense 向量
        x = self.embedding(x)  # (batch_size, seq_len, d_model)

        # 2. 加入位置编码，增强位置感知能力
        x = self.pos_encoder(x)

        # 3. 创建 padding mask，shape：(batch_size, 1, 1, seq_len)
        # mask = True 代表有效 token，False 代表 padding 位置
        pad_mask = (x.sum(-1) != 0).unsqueeze(1).unsqueeze(2)  # (batch_size, 1, 1, seq_len)

        # 4. 依次通过多层 Encoder，每一层都会使用 pad_mask
        for layer in self.layers:
            x = layer(x, pad_mask)

        # 5. 对时间维度（seq_len）做 mean pooling，聚合所有位置的特征
        out = x.mean(dim=1)  # mean pooling on seq_len

        # 6. 分类输出，映射到类别数
        return self.fc(out)
```

> [!TIP]
>
> **思考题**
>
> 思考题5：为什么在 TransformerEncoderClassifier 中，通常会在 Encoder 的输出上做 mean pooling（对 seq_len 取平均）？除了 mean pooling，你能否想到其他可以替代的 pooling 或特征聚合方式？并简要分析它们的优缺点。
>

下面是模型的训练和测试:

```python
# 使用 split 进行分词
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = TransformerEncoderClassifier(len(vocab)).to(device)
optimizer = optim.Adam(model.parameters(), lr=1e-3)
criterion = nn.CrossEntropyLoss()
```

模型训练部分：

```python
def train_epoch():
    model.train()
    total_loss = 0
    loop = tqdm(train_dataloader, desc="Training", leave=False)
    for text, labels in loop:
        text, labels = text.to(device), labels.to(device)
        optimizer.zero_grad()
        output = model(text)
        loss = criterion(output, labels)
        loss.backward()
        optimizer.step()
        total_loss += loss.item()

        # 更新tqdm进度条
        loop.set_postfix(loss=loss.item())
    return total_loss / len(train_dataloader)
```

模型测试部分：

```python
def evaluate():
    model.eval()
    correct = 0
    total = 0
    loop = tqdm(test_dataloader, desc="Evaluating", leave=False)
    with torch.no_grad():
        for text, labels in loop:
            text, labels = text.to(device), labels.to(device)
            output = model(text)
            preds = output.argmax(dim=1)
            correct += (preds == labels).sum().item()
            total += labels.size(0)
    return correct / total


for epoch in range(1, 6):
    loss = train_epoch()
    acc = evaluate()
    print(f'Epoch {epoch}：Loss = {loss:.4f}, Test Acc = {acc:.4f}')
```

> [!TIP]
>
> **思考题**
>
> 思考题6：Transformer 相比传统的 RNN/CNN，优势在哪里？为什么 Transformer 更适合处理长文本？
>

## 实验任务二： 预训练语言模型

> **实验目标**
>
> 通过本次实验，你将掌握以下内容：
>
> 1. 使用GPU训练模型
> 2. 了解预训练语言模型
> 3. 使用预训练语言模型进行文本分类
>

本次实验所用的预训练模型（BERT）下载链接如下：

预训练模型（BERT）下载链接：https://box.nju.edu.cn/d/2710380144234ce78fe3/

### 1. 使用GPU训练模型

在PyTorch中，可以使用以下代码来检测当前环境是否有可用的GPU：

```python
import torch

# 检查是否有可用的GPU
if torch.cuda.is_available():
    print(f"CUDA is available. Number of GPUs：{torch.cuda.device_count()}")
    print(f"Current device：{torch.cuda.current_device()}")
    print(f"Device name：{torch.cuda.get_device_name(torch.cuda.current_device())}")
else:
    print("CUDA is not available. Using CPU.")
```

如果显示'CUDA is not available. Using CPU.'请确认启动的环境是否正确或者尝试重新安装pytorch或者与助教联系。

> [!WARNING]
>
> **GPU训练提示**
>
> 如果要用GPU训练，则需要把数据和模型都放到GPU上才能训练。如果一个在CPU一个在GPU，则会报错。
>

定义模型后，通过model = model.to(device)把模型放到GPU上。 把模型放到GPU上的代码示例：

```python
# 检查是否有可用的GPU
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

# 创建模型
model = SimpleModel()

# 将模型放到GPU（如果可用）
model = model.to(device)
```

由于模型在GPU上，所以数据也必须在GPU上才能送入模型。通过inputs = inputs.to(device)把input放到GPU上。值得说明的是由于模型的输出也在GPU上，所以标签也需要放到GPU上以便于计算损失，通过labels = labels.to(device)。

把数据放到GPU上的代码示例：

```python
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader

# 训练示例
num_epochs = 3
for epoch in range(num_epochs):
    model.train()
    for inputs, labels in train_loader:
        # 将数据放到GPU（如果可用）
        inputs, labels = inputs.to(device), labels.to(device)

        # 前向传播
        outputs = model(inputs)
```

通过上述过程，我们可以把数据和模型都放到GPU上从而加速训练。

你可以使用以下命令查看是否使用了GPU并且观察的GPU利用率：

```
watch -n 5 nvidia-smi
```

这个命令会每5秒（-n 5）更新一次NVIDIA GPU的状态信息。

### 2. 了解预训练语言模型

> **预训练语言模型简介**
>
> 预训练语言模型（pre-trained language models）是指在大规模数据集上预先训练过的语言模型。这些模型已经学习到了一些基础的特征或知识，并可以被迁移到特定的任务上进行微调（fine-tuning）。
>

下面我们以BERT为例，用的bert-base-uncased版本进行实验。我们首先用AutoModel和AutoTokenizer加载模型和分词器。分词器是把文本的每个词元映射到对应的索引，以便于BERT的embedding层完成索引到嵌入的映射。

完整代码如下：

```python
import torch
from transformers import AutoModel, AutoTokenizer

# 指定模型名称
model_name = 'bert-base-uncased'

# 读取模型对应的tokenizer
tokenizer = AutoTokenizer.from_pretrained(model_name)

# 载入模型
model = AutoModel.from_pretrained(model_name)

# 输入文本
input_text = "Here is some text to encode"

# 通过tokenizer把文本变成 token_id
input_ids = tokenizer.encode(input_text, add_special_tokens=True)
print(input_ids)

# 转换为Tensor
input_ids = torch.tensor([input_ids])

# 获得BERT的输出
with torch.no_grad():
    output = model(input_ids)

# 获得BERT模型最后一个隐层结果
output_hidden_states = output.last_hidden_state
output_hidden_states.shape
```

分词（tokenizer）的过程会在文本的头尾添加特殊token，即会在文本的开头加入词元[CLS]并且在文本的结尾加入词元[SEP]。你可以调整input_text和设置add_special_tokens=False，观察到这两个词元分别被编码为101和102。

除此之外，由于批处理过程需要一个批次中文本长度相同，因此额外引入了padding。所以，我们需要使用了attention_mask屏蔽这些padding token，不让其参与自注意力的计算。

最终的输出是文本中所有词元的隐藏状态（hidden states）。

我们可以用model.named_parameters()：观察模型的所有参数及其形状，完整代码如下：

```python
import torch
from transformers import AutoModel, AutoTokenizer

# 指定模型名称
model_name = 'bert-base-uncased'

# 读取模型对应的tokenizer
tokenizer = AutoTokenizer.from_pretrained(model_name)

# 载入模型
model = AutoModel.from_pretrained(model_name)

# 打印模型所有参数的名称和形状
for name, param in model.named_parameters():
    print(f"Parameter Name：{name}, Shape：{param.shape}")
```

### 3. 使用预训练模型进行文本分类

可能需要安装transformers包

```
pip install transformers
```

在本章节中，你将基于上面的BERT代码和AG NEWS数据集进行基于预训练模型BERT的文本分类。你将完善下述代码同时探索多种句子聚合方式对结果的影响，其中句子聚合方式指的是从词嵌入中得到句子嵌入的过程。需要探索的句子聚合方式包括：

1. 直接使用[CLS]的嵌入表示当做句子嵌入。
2. 使用mean-pooling平均一个句子中的所有词元得到嵌入
3. 使用注意力机制给每个词元分配一个权重，通过加权求和的方式得到嵌入。你可以使用任意注意力机制计算。

学习率可以参考设置为2e-5。

```python
import torch
import pandas as pd
import numpy as np
from torch.utils.data import Dataset, DataLoader
from transformers import AutoTokenizer, AutoModelForSequenceClassification, Adam
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
from tqdm import tqdm

# **1. 加载 AG NEWS 数据集**
df = pd.read_csv("train.csv")  # 请替换成你的文件路径
df.columns = ["label", "title", "description"]  # CSV 有3列：标签, 标题, 描述
df["text"] = df["title"] + " " + df["description"]  # 合并标题和描述作为输入文本
df["label"] = df["label"] - 1  # AG NEWS 的标签是 1-4，我们转换成 0-3
train_texts, train_labels = df["text"].tolist(), df["label"].tolist()
number = int(0.3 * len(train_texts))
train_texts, train_labels = train_texts[：number], train_labels[：number]

df = pd.read_csv("test.csv")  # 请替换成你的文件路径
df.columns = ["label", "title", "description"]  # CSV 有3列：标签, 标题, 描述
df["text"] = df["title"] + " " + df["description"]  # 合并标题和描述作为输入文本
df["label"] = df["label"] - 1  # AG NEWS 的标签是 1-4，我们转换成 0-3
test_texts, test_labels = df["text"].tolist(), df["label"].tolist()

# **2. 加载 BERT Tokenizer**
model_name = "bert-base-uncased"
tokenizer = AutoTokenizer.from_pretrained(model_name)

# **3. 处理数据**
class AGNewsDataset(Dataset):
    def __init__(self, texts, labels, tokenizer, max_length=50):
        self.texts = texts
        self.labels = labels
        self.tokenizer = tokenizer
        self.max_length = max_length

    def __len__(self):
        return len(self.texts)

    def __getitem__(self, idx):
        text = self.texts[idx]
        label = self.labels[idx]
        encoding = self.tokenizer(
            text, truncation=True, padding="max_length", max_length=self.max_length, return_tensors="pt"
        )
        return {
            "input_ids"：encoding["input_ids"].squeeze(0),
            "attention_mask"：encoding["attention_mask"].squeeze(0),
            "labels"：torch.tensor(label, dtype=torch.long),
        } # 此处会自动生成BERT输入所需要的attention_mask


train_dataset = AGNewsDataset(train_texts, train_labels, tokenizer)
test_dataset = AGNewsDataset(test_texts, test_labels, tokenizer)

train_dataloader = DataLoader(train_dataset, batch_size=16, shuffle=True)
test_dataloader = DataLoader(test_dataset, batch_size=16, shuffle=False)

# **4. 定义和加载BERT分类模型**
#TODO:定义模型并且放到GPU上

class BERTClassifier(nn.Module):
    def __init__(self, model_name, num_labels):
        super(BERTClassifier, self).__init__()
        self.bert =
        self.classifier = 

    def forward(self, input_ids, attention_mask):

        return logits

model = BERTClassifier(model_name, num_labels=4).to(device)


# **5. 设置优化器和损失函数**
#TODO：定义优化器和损失函数

# **6. 训练 BERT**
EPOCHS = 3

for epoch in range(EPOCHS):
    model.train()
    total_loss = 0
    loop = tqdm(train_dataloader, desc=f"Epoch {epoch+1}")

    for batch in loop:
        #TODO：基于后面需要打印的损失，定义训练过程

    print(f"Epoch {epoch+1}, Loss：{total_loss/len(train_dataloader):.4f}")

    # **7. 评估模型**
    model.eval()
    preds, true_labels = [], []

    with torch.no_grad():
        for batch in test_dataloader:
            #TODO：基于后面计算acc需要的true_labels和preds，完善下面测试代码


    acc = accuracy_score(true_labels, preds)
    print(f"Test Accuracy：{acc:.4f}")
```

> **训练速度**
>
> 你如果觉得训练速度慢，可以尝试增大batch size，不过注意不要炸显存。
>

> [!TIP]
>
> **思考题**
>
> 思考题1：你觉得以上三种得到句子嵌入的方案，哪种效果会最好，哪种效果会最差？为什么？
>
> 思考题2：如果一个文档包括多个句子，我们需要获得其中每个句子的嵌入表示。那么，我们应该怎么利用BERT得到每个句子的嵌入？
>
