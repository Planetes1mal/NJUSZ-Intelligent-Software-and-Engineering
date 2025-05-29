# 实验四：词嵌入和循环神经网络

## 实验任务一： 词嵌入

> **实验目标**
>
> 通过本次实验，你将掌握以下内容：
>
> 认识词嵌入。
> 从Glove词嵌入中，探索一些词嵌入的基本性质。
> 利用词嵌入进行文本分类。

### 1. 词嵌入

> [!NOTE]
>
> **词嵌入简介**
>
> 词嵌入是指用一个低维向量来表示单词。词嵌入被用作自然语言处理任务（如情感分类、问答、翻译等）的基本组成部分。因此，在本次实验中，我们了解词嵌入的构造并且直观感受词嵌入。

本次实验所用的词嵌入和数据集下载链接如下：

词嵌入下载链接：https://box.nju.edu.cn/d/591925358e264f3b9a75/

ag数据下载链接：https://box.nju.edu.cn/f/7d3e4fce48fb446884c9/?dl=1

### 2. 探索词嵌入

在本节，我们将基于训练好的Glove词嵌入(感兴趣的同学可以自行google Glove的论文，GloVe: Global Vectors for Word Representation)进行一些初步探索。首先加载Glove词嵌入

```python
import numpy as np

def load_glove_embeddings(glove_file, embedding_dim=50):
    """
    读取 GloVe 词向量文件，并返回：
    - word_to_vec: 单词到向量的映射
    - word_to_index: 单词到索引的映射
    - index_to_word: 索引到单词的映射
    - embedding_matrix: 词嵌入矩阵
    """
    word_to_vec = {}
    word_to_index = {}
    index_to_word = {}

    # 读取 GloVe 词向量文件
    with open(glove_file, 'r', encoding='utf-8') as f:
        for idx, line in enumerate(f):
            values = line.strip().split()
            word = values[0]  # 取出单词
            vector = np.array(values[1:], dtype=np.float32)  # 取出向量
            word_to_vec[word] = vector
            word_to_index[word] = idx + 1  # 从1开始编号
            index_to_word[idx + 1] = word

    # 创建嵌入矩阵 (词汇大小 x 维度)
    vocab_size = len(word_to_vec) + 1  # +1 是因为从1开始编号
    embedding_matrix = np.zeros((vocab_size, embedding_dim), dtype=np.float32)

    for word, idx in word_to_index.items():
        embedding_matrix[idx] = word_to_vec[word]

    return word_to_vec, word_to_index, index_to_word, embedding_matrix

# 使用示例（请替换 'glove.6B.50d.txt' 为你的GloVe文件路径）
glove_path = "glove.6B.50d.txt"
word_to_vec, word_to_index, index_to_word, embedding_matrix = load_glove_embeddings(glove_path)
# 示例：查看 'king' 的词向量
print("king 的词向量：", word_to_vec.get("king"))
```

#### 2.1 寻找相似的词

请在词汇表中寻找跟king最相似的10个单词并打印这两个单词间的相似度，可以使用余弦相似度度量两个单词的相似性。

```python
import numpy as np
from scipy.spatial.distance import cosine

def find_top_similar_words(target_word, word_to_vec, top_n=5):
    """
    找到离 target_word 最近的 top_n 个单词（基于余弦相似度）

    :param target_word: 目标单词
    :param word_to_vec: 词向量字典 {word: vector}
    :param top_n: 返回最相近的单词数
    :return: [(word, similarity)] 排序后的列表
    """


    return similarities[:top_n]

# 查找 "king" 最相似的 20 个单词
top_words = find_top_similar_words("king", word_to_vec, top_n=5)

# 打印结果
for word, sim in top_words:
    print(f"{word}: {sim:.4f}")
```

#### 2.2 多义词

有一些词往往具有多个意思比如苹果。请先思考一个多义词，并且使用Glove词嵌入进行验证。即Glove中与其最相似的20个单词中是否包含这两个意思的相关单词。最后，请给出这个单词并且打印跟其最相似的20个单词的相似度。

```python
import numpy as np
from scipy.spatial.distance import cosine

def find_top_similar_words(target_word, word_to_vec, top_n=5):
    """
    找到离 target_word 最近的 top_n 个单词（基于余弦相似度）

    :param target_word: 目标单词
    :param word_to_vec: 词向量字典 {word: vector}
    :param top_n: 返回最相近的单词数
    :return: [(word, similarity)] 排序后的列表
    """


    return similarities[:top_n]

# 查找 "king" 最相似的 20 个单词
top_words = find_top_similar_words("king", word_to_vec, top_n=20)

# 打印结果
for word, sim in top_words:
    print(f"{word}: {sim:.4f}")
```

#### 2.3 使用词嵌入表示关系(类比)

有一个著名的例子是: 国王的词嵌入-男人的词嵌入约等于女王的词嵌入-女人的词嵌入，即embedding(国王)-embedding(男人)≈embedding(女王)-embedding(女人)。

基于这个案例，我们可以用embedding(china)-embedding(beijing)定义首都的关系。请基于中国-北京的得到的首都关系向量，找出英国的首都。英国使用2个单词england和britain进行探索，并且打印出相似度最高的10个单词。再用类似的方式找出伦敦(london)为首都对应的国家。

```python
import numpy as np
from scipy.spatial.distance import cosine

def find_top_similar_embeddings(target_embedding, word_to_vec, top_n=10):
    """
    根据一个词向量，找到最相似的 top_n 个单词（基于余弦相似度）

    :param target_embedding: 目标词向量 (numpy 数组)
    :param word_to_vec: 词向量字典 {word: vector}
    :param top_n: 返回最相近的单词数
    :return: [(word, similarity)] 排序后的列表
    """
    similarities = []

    # 遍历所有单词，计算余弦相似度
    for word, vec in word_to_vec.items():
        similarity = 1 - cosine(target_embedding, vec)  # 余弦相似度
        similarities.append((word, similarity))

    # 按相似度排序（降序）
    similarities.sort(key=lambda x: x[1], reverse=True)

    return similarities[:top_n]
#  获得以下单词的词嵌入
england_, china_, beijing_ = word_to_vec.get("england"), word_to_vec.get("china"),  word_to_vec.get("beijing")
```

#### 2.4 词嵌入的不足

请叙述glove词嵌入的不足。言之有理即可，但避免出现一些比较大的阐述且没有分析，如性能一般，训练语料较少等。

### 3. 使用词嵌入进行文本分类

我们接下来将基于Glove词嵌入对AG News数据集进行文本分类。

> [!NOTE]
>
> **AG News 数据集简介**
>
> AG News 数据集来源于 AG's corpus of news articles，是一个大型的新闻数据集，由 Antonio Gulli 从多个新闻网站收集整理。 AG News 数据集包含 4 类新闻，每类 30,000 条训练数据，共 120,000 条训练样本 和 7,600 条测试样本。

#### 3.1 文本预处理

首先导入所需模块：（可能需要安装datasets包）

```
pip install datasets
```

```python
import torch
import torch.nn as nn
import torch.optim as optim
from datasets import load_dataset, load_from_disk
from collections import Counter
from torch.nn.utils.rnn import pad_sequence
import torch.nn.functional as F
from tqdm import tqdm
import os
```

我们从AG News 数据集中加载文本。这是一个较小的语料库，有150000多个单词，但足够我们小试牛刀.

```python
data_path = "ag_news文件夹保存路径"
dataset = load_from_disk(data_path)

# 提取所有文本数据和标签
train_text = [item['text'] for item in dataset['train']]
train_y = [item['label'] for item in dataset['train']]
test_text = [item['text'] for item in dataset['test']]
test_y = [item['label'] for item in dataset['test']]

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
```

词元化 下面的tokenize函数将文本行列表（lines）作为输入，列表中的每个元素是一个文本序列（如一条文本行）。每个文本序列又被拆分成一个词元列表，词元（token）是文本的基本单位。最后，返回一个由词元列表组成的列表，其中的每个词元都是一个字符串（string）。词元的类型是字符串，而模型需要的输入是数字，因此这种类型不方便模型使用。现在，让我们构建一个字典，通常也叫做词表（vocabulary），用来将字符串类型的词元映射到从0开始的数字索引中。这里我们使用之前Glove中定义过的word_to_index。

在这些步骤后，我们顺序地把一段文本映射成了数字，可以送入模型中进行处理。

```python
# 使用 split 进行分词
def tokenize(text):
    return text.lower().split()

def numericalize(text):
    return torch.tensor([word_to_index.get(word, 0) for word in tokenize(text)], dtype=torch.long)


def pad_tensor(tensor, target_length=100, pad_value=0):
    """
    Pads a tensor with the given pad_value up to target_length.

    Args:
        tensor (torch.Tensor): Input 1D tensor.
        target_length (int): Desired length after padding. Default is 100.
        pad_value (int): Value to pad with. Default is 0.

    Returns:
        torch.Tensor: Padded tensor of shape (target_length,).
    """
    current_length = tensor.size(0)
    if current_length >= target_length:
        return tensor[:target_length]  # Truncate if longer
    else:
        padding = torch.full((target_length - current_length,), pad_value, dtype=tensor.dtype)
        return torch.cat((tensor, padding), dim=0)

# 生成训练数据
def create_data(text_list, seq_len=100):
    X = []
    for text in text_list:
        token_ids = numericalize(text)
        # 都处理成长度为100的序列
        token_ids = pad_tensor(token_ids)
        X.append(token_ids)
    return torch.stack(X)


# 生成训练数据
X_train = create_data(train_text, seq_len=100)
Y_train = torch.Tensor(train_y)

# 生成测试数据
X_test = create_data(test_text, seq_len=100)
Y_test = torch.Tensor(test_y)

# 考虑到训练时间 只取前 50% 的数据
subset_size = int(0.5 * len(X_train))  # 计算 50% 的样本数量
X_train = X_train[:subset_size]
Y_train = Y_train[:subset_size]

# 创建 DataLoader
batch_size = 32
train_data = torch.utils.data.TensorDataset(X_train, Y_train)
train_loader = torch.utils.data.DataLoader(train_data, batch_size=batch_size, shuffle=True)

test_data = torch.utils.data.TensorDataset(X_test, Y_test)
test_loader = torch.utils.data.DataLoader(test_data, batch_size=batch_size, shuffle=True)
```

```python
# 使用 split 进行分词
def tokenize(text):
    return text.lower().split()

def numericalize(text):
    return torch.tensor([word_to_index.get(word, 0) for word in tokenize(text)], dtype=torch.long)


def pad_tensor(tensor, target_length=100, pad_value=0):
    """
    Pads a tensor with the given pad_value up to target_length.

    Args:
        tensor (torch.Tensor): Input 1D tensor.
        target_length (int): Desired length after padding. Default is 100.
        pad_value (int): Value to pad with. Default is 0.

    Returns:
        torch.Tensor: Padded tensor of shape (target_length,).
    """
    current_length = tensor.size(0)
    if current_length >= target_length:
        return tensor[:target_length]  # Truncate if longer
    else:
        padding = torch.full((target_length - current_length,), pad_value, dtype=tensor.dtype)
        return torch.cat((tensor, padding), dim=0)

# 生成训练数据
def create_data(text_list, seq_len=100):
    X = []
    for text in text_list:
        token_ids = numericalize(text)
        # 都处理成长度为100的序列
        token_ids = pad_tensor(token_ids)
        X.append(token_ids)
    return torch.stack(X)


# 生成训练数据
X_train = create_data(train_text, seq_len=100)
Y_train = torch.Tensor(train_y)

# 生成测试数据
X_test = create_data(test_text, seq_len=100)
Y_test = torch.Tensor(test_y)

# 考虑到训练时间 只取前 50% 的数据
subset_size = int(0.5 * len(X_train))  # 计算 50% 的样本数量
X_train = X_train[:subset_size]
Y_train = Y_train[:subset_size]

# 创建 DataLoader
batch_size = 32
train_data = torch.utils.data.TensorDataset(X_train, Y_train)
train_loader = torch.utils.data.DataLoader(train_data, batch_size=batch_size, shuffle=True)

test_data = torch.utils.data.TensorDataset(X_test, Y_test)
test_loader = torch.utils.data.DataLoader(test_data, batch_size=batch_size, shuffle=True)
```

#### 3.2 嵌入层

nn.Embedding()是 PyTorch 中用于创建词嵌入层（embedding layer）的模块，通常用于自然语言处理（NLP）任务。它的主要功能是将单词索引映射为稠密的向量表示。

在本次实验中，我们使用self.embedding = nn.Embedding.from_pretrained(embedding_matrix, freeze=True)，其中参数embedding_matrix是Glove的词嵌入，即我们使用Glove的词嵌入来初始化嵌入层；参数freeze表示嵌入层是否会更新参数，我们设置为freeze=True，即不会更新词嵌入。

#### 3.3 文本分类网络

请基于在上文给出的数据处理和词嵌入矩阵，完成以下文本分类代码。包括四个部分，定义文本分类网络，实现训练函数，实现测试函数以及定义损失函数。

```python
#TODO:定义文本分类网络
class TextClassifier(nn.Module):
    def __init__(self, vocab_size, embedding_dim, hidden_dim, num_classes):
        super(TextClassifier, self).__init__()

        #TODO: 实现模型结构
        #TODO 实现self.embedding: 嵌入层
        #TODO 实现self.fc: 分类层


    def forward(self, x):
        x = self.embedding(x)
        #TODO: 对一个句子中的所有单词的嵌入取平均得到最终的文档嵌入
        return self.fc(x)

# TODO: 实现训练函数，注意要把数据也放到gpu上避免报错
def train_model(model, dataloader, criterion, optimizer):


# TODO: 实现测试函数，返回在测试集上的准确率
def evaluate_model(model, dataloader):


# 初始化模型
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
embedding_matrix = torch.Tensor(embedding_matrix)
model = TextClassifier(embedding_matrix).to(device)
#TODO 实现criterion: 定义交叉熵损失函数
optimizer = optim.Adam(model.parameters(), lr=0.001)

# 训练模型
EPOCHS = 5
for epoch in range(EPOCHS):
    train_model(model, train_loader, criterion, optimizer)
    acc = evaluate_model(model, test_loader)
    print(f"Epoch {epoch+1}, Accuracy: {acc*100:.2f}%")
```

> [!TIP]
>
> **思考题**
>
> 使用Glove词嵌入进行初始化，是否比随机初始化取得更好的效果？
>

> [!TIP]
>
> **思考题**
>
> 上述代码在不改变模型（即仍然只有self.embedding和self.fc，不额外引入如dropout等层）和超参数（即batch size和学习率）的情况下，我们可以修改哪些地方来提升模型性能。请列举两个方面。



## 实验任务二: RNN、LSTM和GRU文本生成任务

### 1. 文本预处理

> [!NOTE]
>
> **文本预处理简介**
>
> 文本预处理是在深度学习和自然语言处理（NLP）任务中，对原始文本进行清理、转换和格式化，使其能够被模型理解和处理的过程。
>

> [!WARNING]
>
> **预处理的必要性**
>
> 原始文本可能包含噪声，且文本长度不一致，导致批量训练时需要填充
>

> [!NOTE]
>
> **AG News 数据集简介**
>
> AG News 数据集来源于 AG's corpus of news articles，是一个大型的新闻数据集，由 Antonio Gulli 从多个新闻网站收集整理。 AG News 数据集包含 4 类新闻，每类 30,000 条训练数据，共 120,000 条训练样本 和 7,600 条测试样本。

首先导入所需模块：（可能需要先安装datasets包）

```
pip install datasets
```

```python
import torch
import torch.nn as nn
import torch.optim as optim
from datasets import load_dataset, load_from_disk
from collections import Counter
from torch.nn.utils.rnn import pad_sequence
import torch.nn.functional as F
from tqdm import tqdm
import os
```

我们从AG News 数据集中加载文本。 这是一个较小的语料库，有150000多个单词，但足够我们小试牛刀.

```python
data_path = "ag_news文件夹保存路径"
dataset = load_from_disk(data_path)

# 提取所有文本数据
train_text = [item['text'] for item in dataset['train']]

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
```

词元化 下面的tokenize函数将文本行列表（lines）作为输入， 列表中的每个元素是一个文本序列（如一条文本行）。 每个文本序列又被拆分成一个词元列表，词元（token）是文本的基本单位。 最后，返回一个由词元列表组成的列表，其中的每个词元都是一个字符串（string）。

```python
# 使用 split 进行分词
def tokenize(text):
    return text.lower().split()

# 生成词汇表
counter = Counter()
for text in train_text:
    counter.update(tokenize(text))
```

词元的类型是字符串，而模型需要的输入是数字，因此这种类型不方便模型使用。 现在，让我们构建一个字典，通常也叫做词表（vocabulary）， 用来将字符串类型的词元映射到从0开始的数字索引中。 首先，定义特殊标记（如 代表未知词， 用于序列填充，表示序列开始，表示序列结束）。然后，从 Counter 统计的单词频率列表中提取所有单词，并按频率排序，将其添加到词汇表中。最后，使用 enumerate 为每个单词分配唯一索引，创建一个 word-to-index 映射，方便将文本转换为数值序列供深度学习模型使用。

```python
# 生成词汇表，包含特殊 token
special_tokens = ["<unk>", "<pad>", "<bos>", "<eos>"]
vocab = special_tokens + [word for word, _ in counter.most_common()]
vocab_dict = {word: idx for idx, word in enumerate(vocab)}
```

打印词汇表大小，前10个高频词元及其索引。

```python
print("词汇表大小:", len(vocab_dict))
print("前 10 个最常见的单词及其索引:")
#TODO:打印前10个高频词元及其索引
```

> [!TIP]
>
> **思考题**
>
> 思考题1：在文本处理中，为什么需要对文本进行分词（Tokenization）？
>
> 思考题2：在深度学习中，为什么不能直接使用单词而需要将其转换为索引？

### 2. RNN文本生成实验

> [!NOTE]
>
> **RNN文本生成概述**
>
> 使用RNN进行文本生成任务的核心思想是 根据前面的文本预测下一个单词，然后将预测出的单词作为输入，循环迭代生成完整文本。本实验以AG News 数据为例，给定前100个单词作为输入，预测下一个单词，实现文本生成任务。

> [!WARNING]
>
> **RNN的局限性**
>
> RNN的局限性在于难以记住长距离上下文，容易导致生成内容缺乏连贯性，且可能出现重复或模式化的文本。

![示例图片](./assets/README/rnn.png)

#### 前置代码

首先导入所需模块：

```python
import torch
import torch.nn as nn
import torch.optim as optim
from datasets import load_dataset, load_from_disk
from collections import Counter
from torch.nn.utils.rnn import pad_sequence
import torch.nn.functional as F
from tqdm import tqdm
import os
```

读取数据集

```python
data_path = "ag_news文件夹保存路径"
dataset = load_from_disk(data_path)

# 提取所有文本数据
train_text = [item['text'] for item in dataset['train']]

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
```

文本的预处理

```python
# 使用 split 进行分词
def tokenize(text):
    return text.lower().split()

# 生成词汇表
counter = Counter()
for text in train_text:
    counter.update(tokenize(text))

# 生成词汇表，包含特殊 token
special_tokens = ["<unk>", "<pad>", "<bos>", "<eos>"]
vocab = special_tokens + [word for word, _ in counter.most_common()]
vocab_dict = {word: idx for idx, word in enumerate(vocab)}
```

#### 训练数据生成

将文本数据转换为数值表示，并按100个单词作为输入、下一个单词作为目标的方式构造训练数据。最终生成 X_train（输入序列）和 Y_train（预测目标），用于 RNN 训练文本生成模型。

```python
def numericalize(text):
    return torch.tensor([vocab_dict.get(word, vocab_dict["<unk>"]) for word in tokenize(text)], dtype=torch.long)

# 生成训练数据（输入 100 个词，预测下一个词）
def create_data(text_list, seq_len=100):
    X, Y = [], []
    for text in text_list:
        token_ids = numericalize(text)
        if len(token_ids) <= seq_len:
            continue  # 忽略过短的文本
        for i in range(len(token_ids) - seq_len):
            X.append(token_ids[i:i + seq_len])
            Y.append(token_ids[i + seq_len])
    return torch.stack(X), torch.tensor(Y)

# 生成训练数据
X_train, Y_train = create_data(train_text, seq_len=100)


# 创建 DataLoader
batch_size = 32
train_data = torch.utils.data.TensorDataset(X_train, Y_train)
train_loader = torch.utils.data.DataLoader(train_data, batch_size=batch_size, shuffle=True)
```

> [!TIP]
>
> **思考题**
>
> 思考题3：如果不打乱训练集，会对生成任务有什么影响？

#### RNN 模型构建

实现了一个基于 RNN 的文本生成模型，通过输入文本序列预测下一个单词。

```python
class RNNTextGenerator(nn.Module):
    def __init__(self, vocab_size, embed_dim, hidden_dim, num_layers=2):
        super(RNNTextGenerator, self).__init__()
        self.embedding = nn.Embedding(vocab_size, embed_dim)#将输入的单词索引转换为 embed_dim 维的向量。
        self.rnn = nn.RNN(embed_dim, hidden_dim, num_layers=num_layers, batch_first=True)#构建一个 RNN 层，用于处理序列数据。
        self.fc = nn.Linear(hidden_dim, vocab_size)#将 RNN 隐藏状态 映射到 词汇表大小的向量，用于预测下一个单词的概率分布。

    def forward(self, x, hidden=None):
        #输入 x 形状：(batch_size, seq_len)
        #输出 embedded 形状：(batch_size, seq_len, embed_dim)
        embedded = self.embedding(x)
        #输入 embedded 形状：(batch_size, seq_len, embed_dim)
        #输出 output 形状：(batch_size, seq_len, hidden_dim)（所有时间步的隐藏状态）
        #输出 hidden 形状：(num_layers, batch_size, hidden_dim)（最后一个时间步的隐藏状态）
        output, hidden = self.rnn(embedded, hidden) 
        #只取 最后一个时间步的隐藏状态 output[:, -1, :] 作为输入
        #通过全连接层 self.fc 将隐藏状态转换为词汇表大小的分布（用于预测下一个单词）
        #最终 output 形状：(batch_size, vocab_size)
        output = self.fc(output[:, -1, :])
        return output, hidden
```

定义模型所需参数、实例化模型、损失函数和优化器

```python
embed_dim = 128
hidden_dim = 512  
vocab_size = len(vocab)

model = RNNTextGenerator(vocab_size, embed_dim, hidden_dim, num_layers=2).to(device)
criterion = nn.CrossEntropyLoss()
optimizer = optim.Adam(model.parameters(), lr=0.001)  
```

#### RNN 模型训练

RNN 训练过程

```python
def train_model(model, train_loader, epochs=5):
    model.train()# 将模型设置为训练模式
    for epoch in range(epochs):
        total_loss = 0
        progress_bar = tqdm(train_loader, desc=f"Epoch {epoch + 1}/{epochs}")# 使用 tqdm 创建进度条
        epoch_grad_norm = None

        for X_batch, Y_batch in progress_bar:
            X_batch, Y_batch = X_batch.to(device), Y_batch.to(device)# 将数据移动到指定设备（GPU/CPU）
            optimizer.zero_grad()# 清空上一轮的梯度，防止梯度累积

            output, _ = model(X_batch)# 前向传播，计算模型输出
            loss = criterion(output, Y_batch) # 计算损失函数值
            loss.backward()# 反向传播，计算梯度

            optimizer.step() # 更新模型参数
            total_loss += loss.item()# 累加当前 batch 的损失值
            progress_bar.set_postfix(loss=loss.item())# 在进度条上显示当前 batch 的损失值

        print(f"Epoch {epoch + 1}, Avg Loss: {total_loss / len(train_loader):.4f}")
        # 计算并输出本轮训练的平均损失

# 训练模型
train_model(model, train_loader, epochs=20)  
```

#### RNN 模型测试

RNN 生成文本测试

```python
def generate_text(model, start_text, num_words=100, temperature=1.0):
    model.eval()# 将模型设置为评估模式，禁用 dropout 和 batch normalization
    words = tokenize(start_text)# 对输入文本进行分词，获取初始词列表
    input_seq = numericalize(start_text).unsqueeze(0).to(device)
    # 将文本转换为数值表示，并调整形状以符合模型输入格式（增加 batch 维度），再移动到指定设备（CPU/GPU）

    hidden = None

    for _ in range(num_words): # 生成 num_words 个单词
        with torch.no_grad(): # 在推理时关闭梯度计算，提高效率
            output, hidden = model(input_seq, hidden)# 前向传播，获取模型输出和新的隐藏状态

        # 计算 softmax，并应用温度系数
        logits = output.squeeze(0) / temperature # 对 logits 除以 temperature 调节概率分布的平滑度
        probs = F.softmax(logits, dim=-1) # 计算 softmax 得到概率分布

        # 采样新词
        predicted_id = torch.multinomial(probs, num_samples=1).item()
        # 基于概率分布 随机采样一个词的索引

        next_word = vocab[predicted_id]  # 从词表中查找对应的单词
        words.append(next_word)# 将生成的单词添加到文本列表中

        # 更新输入序列（将新词加入，并移除最旧的词，维持输入长度）
        input_seq = torch.cat([input_seq[:, 1:], torch.tensor([[predicted_id]], dtype=torch.long, device=device)],
                              dim=1)

    return " ".join(words) 

# 生成文本
print("\nGenerated Text:")
test_text = dataset["test"][1]["text"]
# 取前 100 个单词作为前缀
test_prefix = " ".join(test_text.split()[:100])

# 让模型基于该前缀生成 100 个词
generated_text = generate_text(model, test_prefix, 100, temperature=0.8)

print("\n🔹 模型生成的文本：\n")
print(generated_text)
```

#### 困惑度评估

**1. 基本概念** 困惑度（Perplexity, PPL）是衡量语言模型好坏的一个常见指标，它表示模型对测试数据的不确定性，即模型在预测下一个词时的困惑程度。 如果一个模型的困惑度越低，说明它对数据的预测越准确，即更“确信”自己生成的词语；如果困惑度高，说明模型的预测不太确定，可能在多个词之间摇摆不定。

**2. 数学定义**

假设一个句子由$N$个单词组成：
$$
W=(w_1,w_2,\ldots,w_N)L_{total}(\mathbf{w},b)=L_{original}(\mathbf{w},b)+\frac{\lambda}{2}\|\mathbf{w}\|^2
$$
模型给出的概率为：
$$
P(W)=P(w_1,w_2,\ldots,w_N)=P(w_1)P(w_2|w_1)P(w_3|w_1,w_2)...P(w_N|w_1,\ldots,w_{N-1})
$$
那么，困惑度（Perplexity, PPL）定义为：
$$
PPL=P(W)^{-\frac{1}{N}}
$$
或者等价地：
$$
PPL=\exp\left(-\frac{1}{N}\sum_{i=1}^N\log P(w_i|w_1,\ldots,w_{i-1})\right)
$$
其中：

- $P(w_i|w_1,\ldots,w_{i-1})$ 是模型在给定前 $i-1$ 个单词时预测 $w_i$ 的概率
- $N$ 是句子的单词总数

困惑度的最好的理解是“下一个词元的实际选择数的调和平均数”。

- 在最好的情况下，模型总是完美地估计标签词元的概率为1。 在这种情况下，模型的困惑度为1。
- 在最坏的情况下，模型总是预测标签词元的概率为0。 在这种情况下，困惑度是正无穷大。

下面请你按照要求补全计算困惑度的代码

```python
def compute_perplexity(model, test_text, vocab_dict, seq_len=100):
    """
    计算给定文本的困惑度（Perplexity, PPL）

    :param model: 训练好的语言模型（RNN/LSTM）
    :param test_text: 需要评估的文本
    :param vocab_dict: 词汇表（用于转换文本到索引）
    :param seq_len: 评估时的窗口大小
    :return: PPL 困惑度
    """
    model.eval()  # 设为评估模式
    words = test_text.lower().split()

    # 将文本转换为 token ID，如果词不在词表中，则使用 "<unk>"（未知词）对应的索引
    token_ids = torch.tensor([vocab_dict.get(word, vocab_dict["<unk>"]) for word in words], dtype=torch.long)

    # 计算 PPL
    total_log_prob = 0
    num_tokens = len(token_ids) - 1  # 预测 num_tokens 次

    with torch.no_grad():
        for i in range(num_tokens):
            """遍历文本的每个 token，计算其条件概率，最后累加log概率"""
            input_seq = token_ids[max(0, i - seq_len):i].unsqueeze(0).to(device)  # 获取前 seq_len 个单词
            if input_seq.shape[1] == 0:  # 避免 RNN 输入空序列
                continue

            target_word = token_ids[i].unsqueeze(0).to(device)  # 目标单词

            # TODO: 前向传播，预测下一个单词的 logits
            # TODO: 计算 softmax 并取 log 概率
            # TODO: 取目标词的对数概率
            # TODO: 累加 log 概率      

    avg_log_prob = total_log_prob / num_tokens  # 计算平均 log 概率
    perplexity = torch.exp(torch.tensor(-avg_log_prob)) # 计算 PPL，公式 PPL = exp(-avg_log_prob)

    return perplexity.item()


# 示例用法
ppl = compute_perplexity(model, generated_text, vocab_dict)
print(f"Perplexity (PPL): {ppl:.4f}")
```

> [!TIP]
>
> **思考题**
>
> 思考题4：假设你在RNN和LSTM语言模型上分别计算了困惑度，发现RNN的PPL更低。这是否意味着RNN生成的文本一定更流畅自然？如果不是，在什么情况下这两个困惑度可以直接比较？
>
> 思考题5：困惑度是不是越低越好？

### 3. LSTM和GRU文本生成实验

> [!NOTE]
>
> **LSTM文本生成概述**
>
> LSTM（Long Short-Term Memory）是一种改进的 RNN，能够通过 门控机制（遗忘门、输入门、输出门） 有效捕捉长期依赖关系，防止梯度消失和梯度爆炸问题，使其在处理长序列任务时比普通 RNN 更强大。 本实验依旧以AG News 数据为例，给定前100个单词作为输入，预测下一个单词，实现文本生成任务。

![示例图片](./assets/README/lstm.png)

文本的预处理 训练数据生成与前面一致

#### LSTM 模型构建

实现了一个基于 LSTM 的文本生成模型，通过输入文本序列预测下一个单词。

```python
class LSTMTextGenerator(nn.Module):
    def __init__(self, vocab_size, embed_dim, hidden_dim, num_layers=2):
        super(LSTMTextGenerator, self).__init__()
        self.embedding = nn.Embedding(vocab_size, embed_dim)
        self.lstm = nn.LSTM(embed_dim, hidden_dim, num_layers=num_layers, batch_first=True)
        self.fc = nn.Linear(hidden_dim, vocab_size)

    def forward(self, x, hidden=None):
        embedded = self.embedding(x)  # (B, L, embed_dim)
        output, hidden = self.lstm(embedded, hidden)  # (B, L, hidden_dim)
        output = self.fc(output[:, -1, :])  # 只取最后一个时间步的输出进行预测
        return output, hidden
```

定义模型所需参数、实例化模型、损失函数和优化器

```python
embed_dim = 128
hidden_dim = 512  
vocab_size = len(vocab)

model = LSTMTextGenerator(vocab_size, embed_dim, hidden_dim, num_layers=2).to(device)
criterion = nn.CrossEntropyLoss()
optimizer = optim.Adam(model.parameters(), lr=0.001)  
```

#### LSTM 模型训练

LSTM 训练过程

```python
def train_model(model, train_loader, epochs=5):
    model.train()
    for epoch in range(epochs):
        total_loss = 0
        progress_bar = tqdm(train_loader, desc=f"Epoch {epoch + 1}/{epochs}")
        epoch_grad_norm = None

        for X_batch, Y_batch in progress_bar:
            X_batch, Y_batch = X_batch.to(device), Y_batch.to(device)
            optimizer.zero_grad()

            output, _ = model(X_batch)
            loss = criterion(output, Y_batch)
            loss.backward()

            optimizer.step()
            total_loss += loss.item()
            progress_bar.set_postfix(loss=loss.item())

        print(f"Epoch {epoch + 1}, Avg Loss: {total_loss / len(train_loader):.4f}")

# 训练模型
train_model(model, train_loader, epochs=20)
```

#### LSTM 模型测试

LSTM 生成文本测试

```python
def generate_text(model, start_text, num_words=100, temperature=1.0):
    model.eval()
    words = tokenize(start_text)
    input_seq = numericalize(start_text).unsqueeze(0).to(device)
    hidden = None

    for _ in range(num_words):
        with torch.no_grad():
            output, hidden = model(input_seq, hidden)

        # 计算 softmax，并应用温度系数
        logits = output.squeeze(0) / temperature
        probs = F.softmax(logits, dim=-1)

        # 采样新词
        predicted_id = torch.multinomial(probs, num_samples=1).item()

        next_word = vocab[predicted_id]
        words.append(next_word)

        input_seq = torch.cat([input_seq[:, 1:], torch.tensor([[predicted_id]], dtype=torch.long, device=device)],
                              dim=1)

    return " ".join(words) 

# 生成文本
print("\nGenerated Text:")
test_text = dataset["test"][1]["text"]
# 取前 100 个单词作为前缀
test_prefix = " ".join(test_text.split()[:100])

# 让模型基于该前缀生成 100 个词
generated_text = generate_text(model, test_prefix, 100, temperature=0.8)
print("\n🔹 模型生成的文本：\n")
print(generated_text)
```

借助RNN文本生成任务中计算困惑度的函数，计算一下lstm在generated_text上的困惑度。

> [!TIP]
>
> **思考题**
>
> 思考题6：观察一下RNN和LSTM训练过程中loss的变化，并分析一下造成这种现象的原因。

> [!NOTE]
>
> **GRU文本生成概述**
>
> GRU（Gated Recurrent Unit）是 LSTM 的简化版本，使用 更新门（Update Gate）和重置门（Reset Gate） 来控制信息流动，计算效率更高，且能在许多任务中取得与 LSTM 相似的效果，同时减少计算成本和参数量。 本实验依旧以AG News 数据为例，给定前100个单词作为输入，预测下一个单词，实现文本生成任务。

![示例图片](./assets/README/GRU.png)

文本的预处理 训练数据生成与前面一致

#### GRU 模型构建

实现了一个基于 GRU 的文本生成模型，通过输入文本序列预测下一个单词。

```python
class GRUTextGenerator(nn.Module):
    def __init__(self, vocab_size, embed_dim, hidden_dim, num_layers=2):
        super(GRUTextGenerator, self).__init__()
        self.embedding = nn.Embedding(vocab_size, embed_dim)
        self.gru = nn.GRU(embed_dim, hidden_dim, num_layers=num_layers, batch_first=True)
        self.fc = nn.Linear(hidden_dim, vocab_size)

    def forward(self, x, hidden=None):
        embedded = self.embedding(x)  # (B, L, embed_dim)
        output, hidden = self.gru(embedded, hidden)  # (B, L, hidden_dim)
        output = self.fc(output[:, -1, :])  # 只取最后一个时间步的输出进行预测
        return output, hidden
```

定义模型所需参数、实例化模型、损失函数和优化器

```python
embed_dim = 128
hidden_dim = 512  
vocab_size = len(vocab)

model = GRUTextGenerator(vocab_size, embed_dim, hidden_dim, num_layers=2).to(device)
criterion = nn.CrossEntropyLoss()
optimizer = optim.Adam(model.parameters(), lr=0.001)
```

#### GRU 模型训练

GRU 训练过程也与LSTM保持一致

#### GRU 模型测试

GRU 生成文本测试

```python
def generate_text(model, start_text, num_words=100, temperature=1.0):
    model.eval()
    words = tokenize(start_text)
    input_seq = numericalize(start_text).unsqueeze(0).to(device)
    hidden = None

    for _ in range(num_words):
        with torch.no_grad():
            output, hidden = model(input_seq, hidden)

        # 计算 softmax，并应用温度系数
        logits = output.squeeze(0) / temperature
        probs = F.softmax(logits, dim=-1)

        # 采样新词
        predicted_id = torch.multinomial(probs, num_samples=1).item()

        next_word = vocab[predicted_id]
        words.append(next_word)

        input_seq = torch.cat([input_seq[:, 1:], torch.tensor([[predicted_id]], dtype=torch.long, device=device)],
                              dim=1)

    return " ".join(words) 

# 生成文本
print("\nGenerated Text:")
test_text = dataset["test"][1]["text"]
# 取前 100 个单词作为前缀
test_prefix = " ".join(test_text.split()[:100])

# 让模型基于该前缀生成 100 个词
generated_text = generate_text(model, test_prefix, 100, temperature=0.8)
print("\n🔹 模型生成的文本：\n")
print(generated_text)
```

借助RNN文本生成任务中计算困惑的函数，计算一下GRU在generated_text上的困惑度。

> [!TIP]
>
> **思考题**
>
> 思考题7：这三个困惑度可以直接比较吗？分析一下。
>
> 思考题8：GRU 只有两个门（更新门和重置门），相比 LSTM 少了一个门控单元，这样的设计有什么优缺点？
>
> 思考题9：在低算力设备（如手机）上，RNN、LSTM 和 GRU 哪个更适合部署？为什么？
>
> 思考题10：如果就是要使用RNN模型，原先的代码还有哪里可以优化的地方？请给出修改部分代码以及实验结果。