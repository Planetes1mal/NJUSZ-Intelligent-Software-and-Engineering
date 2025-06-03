# 1 Morris 计数器

## 1.1 Morris 计数器的直觉

现在需要统计一个非常大的事件数量 `n`（比如网站的点击次数），但内存非常有限，甚至无法存储 `n` 本身（`n` 可能需要很多位来表示）。Morris 计数器的目标就是用极小的内存（对数级别，甚至是 $\log \log n$ 级别）来得到 `n` 的一个近似估计

算法描述：维护一个计数器 $X$，初始化为 0。当一个事件到达时，以概率 $p_X = 2^{-X}$ 将计数器 $X$ 加 1。即 $X \leftarrow X + 1 \text{ w.p. } 1/2^X$。当需要估计总次数 $n$ 时，使用估计量 $\hat{n} = 2^X - 1$。

直觉解释

* 计数器 $X$ 的增长速度远慢于实际事件数 $n$。如果 $n$ 约为 $2^k$，那么 $X$ 的期望值约为 $k = \log_2 n$。存储 $X$ 只需要 $O(\log k）= O(\log \log n)$ 比特，满足了空间限制要求
* $X$ 可以看作是对真实计数值 $n$ 的以 2 为底的对数量级的近似。$X$ 的每一次增加大致对应于 $n$ 翻倍
* 概率 $p_X = 1/2^X$ 的设计是有意义的。当 $X$ 的值较小时，增加 $X$ 的概率较大；当 $X$ 增大时，概率指数级减小。具体地，从 $X$ 增加到 $X+1$ 平均需要 $1/p_X = 2^X$ 次事件（发生在 $X$ 保持不变期间的事件之后）。这恰好反映了计数值翻倍的模式：我们期望在计数值从大约 $2^X$ 增长到大约 $2^{X+1}$ 的过程中，$X$ 增加 1
* 既然平均需要 $2^i$ 次事件才能使计数器从 $i$ 增加到 $i+1$，那么当计数器达到值 $X$ 时，累积的期望事件数大约是 $\sum_{i=0}^{X-1} 2^i = 1 + 2 + 4 + \dots + 2^{X-1} = 2^X - 1$。因此，$\hat{n} = 2^X - 1$ 被用作 $n$ 的（近似）无偏估计量

## 1.2 应用 $(1+\alpha)^{-X}$ 的思路

引入一个参数 $\alpha > 0$。将计数器 $X$ 的增加概率修改为 $p_X =（1+\alpha)^{-X}$

修改后的算法：选择参数 $\alpha > 0$。初始化计数器 $X=0$。当一个事件到达时，以概率 $p_X =（1+\alpha)^{-X}$ 将计数器 $X$ 加 1。估计总次数 $n$ 时，使用新的估计量 $\hat{n}_\alpha$

使计数器从 $i$ 增加到 $i+1$ 平均需要的事件数是 $1/p_i =（1+\alpha)^i$。当计数器值为 $X$ 时，累积的期望事件数 $\hat{n}_\alpha$ 约为：

$$
\begin{aligned}
\hat{n}_\alpha &= \sum_{i=0}^{X-1} (1+\alpha)^i \\
&= \frac{(1+\alpha)^0 [(1+\alpha)^X - 1]}{(1+\alpha) - 1} \\
&= \frac{(1+\alpha)^X - 1}{\alpha} 
\end{aligned}
$$

参数 $\alpha$ 控制了计数的“基数” $b = 1+\alpha$

* 当 $0 < \alpha < 1$ 时，$1 < b < 2$。这意味着增加 $X$ 的概率 $p_X = b^{-X}$ 比原始 Morris Counter（$b=2$）下降得**更慢**
* 当 $\alpha > 1$ 时，$b > 2$，概率 $p_X$ 下降得**更快**

当 $0 < \alpha < 1$ 时，基数 $b=1+\alpha$ 更接近 1。这意味着计数器 $X$ 的每一次增加代表着真实计数值 $n$ 的一个较小的乘性增长（乘以 $b$ 而不是 2）。计数的粒度变得更细

Morris 类计数器的相对标准误差 $\text{StdDev}(\hat{n}/n)$ 与 $\sqrt{\alpha}$（或相关参数）成正比。选择较小的 $\alpha$（即 $0 < \alpha < 1$）会**降低估计的相对误差**，从而提高精度。直观地看，更细粒度的计数（更频繁但更小的概率性步骤）减少了随机性对最终估计值产生的相对波动

为了达到相同的估计值 $\hat{n}_\alpha \approx n$，当 $\alpha$ 较小时，需要更大的计数器值 $X$。从 $\hat{n}_\alpha \approx n$ 可得 $(1+\alpha)^X \approx \alpha n + 1$。两边取对数，$X \log(1+\alpha）\approx \log(\alpha n + 1)$。因此 $X \approx \frac{\log(\alpha n + 1)}{\log(1+\alpha)}$。对于小的 $\alpha$，$\log(1+\alpha）\approx \alpha$，所以 $X \approx \frac{\log n}{\alpha}$（粗略估计）。相比之下，原始 Morris Counter 的 $X \approx \log_2 n = \frac{\log n}{\log 2}$。如果 $\alpha < \log 2 \approx 0.693$，则广义计数器的 $X$ 值会更大。这意味着需要**略微更多的存储空间**来存储 $X$（$O(\log X）= O(\log(\frac{\log n}{\alpha}))$ 比特)。然而，由于 $\log \log$ 函数增长极其缓慢，这种空间上的微小增加通常被认为是值得的，以换取估计精度的显著提升



# 2 Median Trick

## 2.1 FM-Median

1. **参数：**选择重复次数 $k$（通常为奇数）
2. **初始化：**选取 $k$ 个**独立**的哈希函数 $h_1, h_2, ..., h_k$，这些函数将元素映射到 $\{0, 1\}^L$。初始化 $k$ 个寄存器 $R_1, R_2, ..., R_k$ 为 0
3. **流处理：**对于数据流中的每一个元素 $x$，对于 $j$ 从 1 到 $k$：
   * 计算 $v_j = h_j(x)$。
   * 计算 $p_j = \rho(v_j)$。
   * 更新 $R_j \leftarrow \max(R_j, p_j)$。
4. **估计：**处理完所有流元素后：
   * 计算 $k$ 个独立的 FM 基础估计值：$\hat{N}_j = c \cdot 2^{R_j}$，对于 $j = 1, ..., k$
   * 最终的基数估计值为 $\hat{N}_{final} = \text{median}(\hat{N}_1, \hat{N}_2, ..., \hat{N}_k)$

## 2.2 正确性分析

每个基础 FM 估计 $\hat{N}_j$ 是 $N$ 的近似无偏估计，但具有 $O(1)$ 的相对方差。这意味着 $P(|\hat{N}_j - N| > \epsilon_0 N)$ 是一个常数 $p$，对于某个由 FM 算法内禀方差决定的常数 $\epsilon_0 > 0$。通过选择合适的 $\epsilon_0$，可以确保 $p < 1/2$

最终估计 $\hat{N}_{final}$ 只有在超过半数（即至少 $\lceil k/2 \rceil$ 个）的基础估计 $\hat{N}_j$ 是“坏”（即偏离 $N$ 超过 $\epsilon_0 N$）的情况下才会是“坏”的

设 $X_j$ 是指示 $\hat{N}_j$ 是否为“坏”估计的 0-1 随机变量，$P(X_j=1)=p < 1/2$。设 $X = \sum_{j=1}^k X_j$ 是“坏”估计的总数。$X$ 服从二项分布 $B(k, p)$。我们关心 $P(X \ge k/2)$。根据 Chernoff 界（适用于二项分布尾概率），当 $p < 1/2$ 时，$P(X \ge k/2)$ 随 $k$ 的增加呈指数级下降：

$$
P(X \ge k/2) \le e^{-k \cdot D(1/2 || p)} \le e^{-k \cdot \text{const}}
$$

其中 $D(1/2 || p)$ 是 $p$ 和 $1/2$ 之间的 KL 散度，是一个大于 0 的常数

为了使最终估计失败（即 $|\hat{N}_{final} - N| > \epsilon_0 N$）的概率小于 $\delta$，我们需要 $e^{-k \cdot \text{const}} \le \delta$，解得 $k \ge \frac{\ln(1/\delta)}{\text{const}}$。因此，选择 $k = O(\log(1/\delta))$ 次重复，可以使得：

$$
P(|\hat{N}_{final} - N| \le \epsilon_0 N) \ge 1 - \delta
$$

其中 $\epsilon_0$ 是由基础 FM 算法的方差决定的一个常数

因此，FM-Median 算法通过 Median Trick 显著提高了估计的可靠性。它不能像 HLL 等算法那样通过参数自由控制 $\epsilon$，但能以 $O(\log(1/\delta))$ 的代价将估计值落在真实值 $N$ 的某个常数因子 $\epsilon_0$ 范围内的概率提高到 $1-\delta$

## 2.3 复杂度分析

设 $M$ 为流中元素的总数， $N$ 为真实基数，$L$ 为哈希函数输出的位数（通常为常数，如 32 或 64）

* **空间复杂度：**
  * 需要存储 $k$ 个寄存器 $R_1, ..., R_k$。每个 $R_j$ 的值最大为 $L$
  * 存储每个 $R_j$ 需要 $O(\log L)$ 位
  * 总空间复杂度为 $O(k \log L)$。由于 $k = O(\log(1/\delta))$ 且 $L$ 是常数，空间复杂度为 $O(\log(1/\delta))$
* **时间复杂度：**
  * **初始化：**选择 $k$ 个独立哈希函数。若使用通用哈希族，时间通常为 $O(k)$ 或可忽略
  * **流处理：**对于流中的 $M$ 个元素，每个元素需要
    * 计算 $k$ 次哈希：$O(k)$ （假设单次哈希为 $O(1)$）
    * 计算 $k$ 次 $\rho$ 值：$O(k)$ （通常为 $O(1)$ 操作）
    * 进行 $k$ 次比较和更新：$O(k)$
    * 处理单个元素的总时间为 $O(k)$
    * 处理整个流的总时间为 $O(M \cdot k）= O(M \log(1/\delta))$
  * **最终估计：**
    * 计算 $k$ 个基础估计值：$O(k)$ 次指数运算
    * 找到 $k$ 个数的中位数：使用线性时间选择算法为 $O(k)$；排序需要 $O(k \log k)$
    * 最终估计时间主要为 $O(k）= O(\log(1/\delta))$
  * **总体时间复杂度：**由流处理主导，为 $O(M \log(1/\delta))$



# 3 HyperLogLog

HLL 的正确性主要体现在其估计量 $\hat{N}$ 的统计特性上：近似无偏性和低相对误差

HLL 继承自 Flajolet-Martin（FM）算法的思想：对于随机均匀的哈希值，观察到的最大 $\rho$ 值（或类似的模式，如 NLZ）与基数 $N$ 的对数（$\log_2 N$）相关。具体来说，观察到 $\rho(w'）\ge k$ 的概率约为 $2^{-k}$。仅使用单个哈希函数和单个最大 $\rho$ 值（如原始 FM）会导致估计方差很大

HLL 的关键改进是使用 $m$ 个寄存器。通过哈希值的前 $b$ 位将数据流随机地划分到 $m$ 个子流（桶）中。每个寄存器 $M[j]$ 相当于对落入该桶的约 $N/m$ 个元素的基数进行独立的（近似的）LogLog 式估计。通过综合 $m$ 个寄存器的信息，可以有效地平均掉由单个哈希值的随机性带来的噪声，从而**显著降低估计的方差**。

为了合并 $m$ 个寄存器 $M[j]$ 的信息，HLL 采用基于调和平均数的指示器 $Z =（\sum 2^{-M[j]})^{-1}$。相比于算术平均数（$\frac{1}{m}\sum 2^{M[j]}$），调和平均数对异常大的 $M[j]$ 值（会导致 $2^{M[j]}$ 极大）**不敏感**（鲁棒性好），因为 $2^{-M[j]}$ 项会变得很小，对总和 $\sum 2^{-M[j]}$ 影响有限。这有助于产生更稳定的估计值

经过常数 $\alpha_m$ 和 $m^2$ 因子修正后的原始估计量 $\hat{N}_{raw} = \alpha_m m^2 Z$ 具有以下关键统计特性：

* **近似无偏性：** $E[\hat{N}_{raw}] \approx N$。
* **相对标准误差（RSE)：** 估计值的相对标准误差（标准差与真实值的比率）主要由寄存器数量 $m$ 决定：

  $$
  text{RSE}(\hat{N}_{raw}) = \frac{\text{StdDev}(\hat{N}_{raw})}{N} \approx \frac{\sigma_{\infty}}{\sqrt{m}}
  $$

  其 $\sigma_{\infty}$ 是一个已知的常数，约为 $1.04$

这个 RSE 公式是 HLL 算法正确性的核心定量表述。它表明 HLL 的**相对精度是可控的**，用户可以通过选择 $m$（即选择参数 $b$）来权衡内存使用和估计精度。$m$ 越大，$\sqrt{m}$ 越大，相对误差越小

重要的是，相对误差 $\approx 1.04/\sqrt{m}$ **不依赖于**真实基数 $N$ 的大小（只要 $N$ 在算法的有效工作范围内）。这意味着 HLL 对各种规模的数据集都能提供一致的相对精度



# 4 Fast Count Min Sketch

## 4.1 算法描述

对于一个由 $M$ 个元素组成的数据流 $S = \langle a_1, a_2, ..., a_M \rangle$，其中每个元素 $a_i$ 来自一个有限或无限的宇宙 $U$，目标是估计流中任意指定项 $item \in U$ 的频率 $f_{item} = |\{i \mid a_i = item\}|$

算法使用两个主要参数：宽度 $w$ 和深度 $d$。这两个参数与期望的误差界限相关

核心数据结构是一个二维数组（或视为 $d$ 个一维数组），记为 `sketch[d][w]`。这是一个包含 $d \times w$ 个计数器的表格，所有计数器初始值设为 0

选择 $d$ 个哈希函数 $h_1, h_2, ..., h_d$。这些函数需要从一个**成对独立 (pairwise independent)** 的哈希函数族 $\mathcal{H}$ 中独立选取。每个函数 $h_j: U \to \{0, 1, ..., w-1\}$ 将宇宙 $U$ 中的元素映射到 $w$ 个桶索引之一

对于任意 $j \in \{1..d\}$，任意不同的 $x, y \in U$，以及任意 $k_1, k_2 \in \{0..w-1\}$，$P(h_j(x)=k_1 \land h_j(y)=k_2) = P(h_j(x)=k_1)P(h_j(y)=k_2)$。通常还假设哈希函数是近似均匀的，即 $P(h_j(x)=k) \approx 1/w$

- 更新：当流中出现一个项 `item` 时（有时也处理带权重的更新 $(item, c)$，这里为简化设 $c=1$）：对于所有的 $j$ 从 1 到 $d$：
  - 计算哈希值 $idx_j = h_j(item)$
  - 增加对应的计数器：`sketch[j][idx_j] += 1`。

* 查询：估计项 `item` 的频率 $\hat{f}_{item}$：对于所有的 $j$ 从 1 到 $d$：

  * 计算哈希值 $idx_j = h_j(item)$。
  * 读取对应的计数器值 $v_j = \text{sketch}[j][idx_j]$。
  * 返回这些值的**最小值**：

    $$
    at{f}_{item} = \min_{j=1..d} v_j = \min_{j=1..d} \text{sketch}[j][h_j(\text{item})]
    $$

## 4.2 正确性分析

设 $f_x$ 为项 $x$ 在流中的真实频率

* **性质 1：估计值 $\hat{f}_{item}$ 总是非负误差（即 $\hat{f}_{item} \ge f_{item}$）**

  * 考虑任意一个计数器 `sketch[j][h_j(item)]`。当项 `item` 每次出现在流中时，这个计数器会增加 1。因此，真实频率 $f_{item}$ 完全贡献给了这个计数器的值
  * 此外，对于任何其他项 $x \neq item$，如果 $h_j(x) = h_j(item)$，那么项 $x$ 的每次出现也会导致 `sketch[j][h_j(item)]` 增加 1
  * 因此，计数器的最终值可以表示为：
    $$
    ext{sketch}[j][h_j(\text{item})] = f_{item} + \sum_{x \neq item, h_j(x)=h_j(item)} f_x
    $$
  * 由于所有频率 $f_x$ 都是非负的，所以 $\sum_{x \neq item, h_j(x)=h_j(item)} f_x \ge 0$
  * 这意味着对于所有的 $j \in \{1..d\}$，都有 $\text{sketch}[j][h_j(\text{item})] \ge f_{item}$
  * 查询结果是这些值的最小值，所以 $\hat{f}_{item} = \min_{j} \text{sketch}[j][h_j(\text{item})] \ge f_{item}$
  * **结论：Count-Min Sketch 永不低估真实频率**
* **性质 2：误差的概率界限 $(\epsilon, \delta)$**

  * 我们希望证明，通过适当地选择 $w$ 和 $d$，可以保证估计误差 $\hat{f}_{item} - f_{item}$ 以高概率被界定。具体来说，对于给定的误差参数 $\epsilon > 0$ 和失败概率 $\delta > 0$，我们希望 $P(\hat{f}_{item} - f_{item} \ge \epsilon ||f||_1) \le \delta$，其中 $||f||_1 = \sum_{x \in U} f_x = M$ 是流的总长度（或所有项频率之和）
  * 令 $X_j = \text{sketch}[j][h_j(\text{item})] - f_{item} = \sum_{x \neq item, h_j(x)=h_j(item)} f_x$。这是第 $j$ 行的噪声或误差。我们已知 $X_j \ge 0$
  * 计算 $X_j$ 的期望值：

    $$
    X_j] = E\left[\sum_{x \neq item, h_j(x)=h_j(item)} f_x\right]
    $$

    由于 $h_j$ 是从成对独立的哈希族中选取的，对于 $x \neq item$，$P(h_j(x)=h_j(item)) \le 1/w$ （如果哈希函数是均匀的，则为 $1/w$）

    $$
    X_j] = \sum_{x \neq item} f_x \cdot P(h_j(x)=h_j(item)) \le \sum_{x \neq item} f_x \cdot \frac{1}{w} = \frac{||f||_1 - f_{item}}{w} \le \frac{||f||_1}{w}
    $$
  * 应用马尔可夫不等式 (Markov's Inequality) 到非负随机变量 $X_j$ 上：

    $$
    X_j \ge \epsilon ||f||_1) \le \frac{E[X_j]}{\epsilon ||f||_1} \le \frac{||f||_1 / w}{\epsilon ||f||_1} = \frac{1}{\epsilon w}
    $$

    这个结果表明，在单行 $j$ 中，误差超过 $\epsilon ||f||_1$ 的概率最多为 $1/(\epsilon w)$
  * 最终的估计误差 $\hat{f}_{item} - f_{item} = (\min_{j} (f_{item} + X_j)) - f_{item} = \min_{j} X_j$
    因此，事件 $\{\hat{f}_{item} - f_{item} \ge \epsilon ||f||_1\}$ 等价于事件 $\{\min_{j} X_j \ge \epsilon ||f||_1\}$，这又等价于事件 $\{X_j \ge \epsilon ||f||_1 \text{ for all } j=1, ..., d\}$。
  * 由于 $d$ 个哈希函数 $h_1, ..., h_d$ 是独立选择的，所以随机变量 $X_1, ..., X_d$ （以及对应的事件）是相互独立的
  * 因此，

    $$
    \hat{f}_{item} - f_{item} \ge \epsilon ||f||_1) = P(X_1 \ge \epsilon ||f||_1 \land \dots \land X_d \ge \epsilon ||f||_1)$$       $$= \prod_{j=1}^d P(X_j \ge \epsilon ||f||_1) \le \left(\frac{1}{\epsilon w}\right)^d
    $$
  * 为了使这个概率小于等于 $\delta$，我们设定参数 $w$ 和 $d$ 如下：

    1. 选择 $w = \lceil e/\epsilon \rceil$。这里 $e \approx 2.718$ 是自然对数的底。这样 $\epsilon w \ge e$，所以 $1/(\epsilon w) \le 1/e$
    2. 选择 $d = \lceil \ln(1/\delta) \rceil$。这样 $d \ge \ln(1/\delta)$
  * 代入这些选择：

    $$
    \hat{f}_{item} - f_{item} \ge \epsilon ||f||_1) \le \left(\frac{1}{e}\right)^d = e^{-d} \le e^{-\ln(1/\delta)} = e^{\ln(\delta)} = \delta
    $$
  * **结论：通过设置 $w = \lceil e/\epsilon \rceil$ 和 $d = \lceil \ln(1/\delta) \rceil$，Count-Min Sketch 保证 $f_{item} \le \hat{f}_{item}$，并且 $\hat{f}_{item}$ 超出 $f_{item}$ 的量大于 $\epsilon ||f||_1$ 的概率不超过 $\delta$**

## 4.3 复杂度分析

* **空间复杂度：**
  * 算法需要存储 $d \times w$ 个计数器。假设每个计数器占用固定空间，则总空间复杂度为 $O(d \cdot w)$
  * 根据我们为满足 $(\epsilon, \delta)$ 保证而选择的参数 $w = O(1/\epsilon)$ 和 $d = O(\log(1/\delta))$，空间复杂度为 $O\left(\frac{1}{\epsilon} \log \frac{1}{\delta}\right)$。这是亚线性的，不依赖于流的大小 $M$ 或宇宙的大小 $|U|$ （除了哈希函数本身可能需要的少量空间）
* **时间复杂度：**
  * **更新：**处理流中的每个元素需要计算 $d$ 个哈希值和执行 $d$ 次计数器（内存）访问和加法操作。假设单次哈希计算和内存访问为 $O(1)$ 时间，则每次更新的时间复杂度为 $O(d) = O(\log(1/\delta))$。处理整个长度为 $M$ 的流的总时间为 $O(M \log(1/\delta))$
  * **查询：**估计一个项的频率需要计算 $d$ 个哈希值，读取 $d$ 个计数器值，并找到这 $d$ 个值的最小值。时间复杂度为 $O(d) = O(\log(1/\delta))$



# 5 Filter 设计

## 5.1 设计细节

- **数据结构**：底层采用一个哈希表，存储 `<Element, Count>` 键值对
- **哈希函数**：选择一个合适的哈希函数 `hash(element)`，将元素映射到哈希表的桶（bucket）索引
- **冲突处理**：采用常见的冲突解决方法，如分离链接法（Separate Chaining，每个桶维护一个链表或平衡树）或开放地址法（Open Addressing，如线性探测、二次探测）

## 5.2 操作逻辑

- **添加元素 `add(x)`**：
  1. 计算元素 `x` 的哈希值，定位到对应桶
  2. 在桶内查找是否存在键为 `x` 的条目
  3. 如果找到，将该条目的计数值（Value）加 1
  4. 如果未找到，在桶内（或通过开放地址法找到新槽位）插入新的键值对 `<x, 1>`
  5. 检查哈希表的负载因子（Load Factor），如果超过阈值，则执行扩容（rehashing）操作以维持性能
- **删除元素 `delete(x)`**：
  1. 计算元素 `x` 的哈希值，定位到对应桶
  2. 在桶内查找是否存在键为 `x` 的条目
  3. 如果找到：
     - 若计数值大于 1，将计数值减 1
     - 若计数值等于 1，将该键值对 `<x, 1>` 从哈希表中彻底移除
  4. 如果未找到，则不执行任何操作（或报告元素不存在）
  5. 检查负载因子，如果过低，可执行缩容操作以节省空间
- **查询元素是否存在 `contains(x)`**：
  1. 计算元素 `x` 的哈希值，定位到对应桶
  2. 在桶内查找是否存在键为 `x` 的条目
  3. 如果找到且其计数值大于 0，则返回 `true`
  4. 否则（未找到或计数值为0），返回 `false`

## 5.3 复杂度分析

- **时间复杂度**：
  - **平均情况**：对于 `add`, `delete`, `contains` 操作，在哈希函数选择良好、负载因子控制在合理范围内的假设下，平均时间复杂度均为 $O(1)$。这是因为哈希计算、桶定位以及桶内（通常很短的链表或数组）的操作平均耗时为常数。动态调整大小（rehashing）虽然单次耗时 $O(N)$（N为当前元素总数，或唯一元素数u，取决于实现），但其成本被分摊到多次操作中，使得平均（摊销）复杂度仍为 $O(1)$
  - **最坏情况**：当发生严重哈希冲突，所有（或大量）元素都映射到同一个桶时，哈希表退化为对桶内数据结构（如链表）的操作。此时，`add`, `delete`, `contains` 的最坏时间复杂度均为 $O(u)$，其中 *u* 是哈希表中不同元素的数量。如果桶内使用平衡树，最坏情况可优化至 $O(\log u)$ 。
- **空间复杂度**：空间复杂度主要取决于存储的不同元素的数量 *u*。哈希表需要存储每个唯一元素及其对应的计数器。此外，哈希表本身（桶数组）也需要空间。因此，空间复杂度为 $O(u+M)$，其中 *M* 是桶的数量。在动态调整大小的哈希表中，通常保持 M 与 u 成比例（维持负载因子），故空间复杂度为 $O(u)$



# 6 Power of $d$ Choices

**结论**

使用 Power of $d$ Choices 策略将 $n$ 个球投入 $n$ 个箱子，当 $n \to \infty$ 时，最终的最大负载 $L_{max}$ 满足：

$$
L_{max} = \frac{\ln \ln n}{\ln d} + O(1)
$$

以高概率（with high probability, w.h.p.）成立

**证明**

1. **定义：**

   - $L_i(t)$：第 $i$ 个箱子在放置 $t$ 个球后的负载
   - $L(t）= \max_{i} L_i(t)$：放置 $t$ 个球后的最大负载
   - $N_k(t)$：在放置 $t$ 个球后，负载至少为 $k$ 的箱子数量
   - $\beta_k(t）= N_k(t）/ n$：在放置 $t$ 个球后，负载至少为 $k$ 的箱子所占的比例
   - $B_k$：表示那些被放入一个**已经**包含至少 $k-1$ 个球的箱子中的球的集合。即，这些球是将某个箱子的负载从 $k-1$ 增加到 $k$（或更高）的球
2. **关键引理：**存在一个足够大的常数 $k_0$，使得对于所有 $k \ge k_0$ 和 $t \le n$，以下事件 $E_k(t)$ 以高概率 $1 - O(n^{-c})$ 成立：

   $$
   N_k(t) \le n \cdot \left( C \cdot \beta_{k-1}(t) \right)^d
   $$

   其中 $C$ 是一个普适常数

   更具体地，w.h.p. 对于所有 $k \ge k_0$：

   $$
   |B_k| \le \gamma \cdot n \cdot (\beta_{k-1}(n))^d
   $$

   其中 $\gamma$ 是一个常数。由于 $N_k(n）\le |B_k|$ （因为每个负载至少为 $k$ 的箱子必须至少接收过一个使其负载达到 $k$ 的球），我们可以推导出关于 $N_k(n)$ 的界限。为简化论证，我们采用如下形式的递归关系：

   w.h.p., 对于所有充分大的 $k$：

   $$
   \beta_k(n) \le 2 (\beta_{k-1}(n))^d
   $$
3. **递归分析：**

   1. **基准情况：**系统的平均负载是 $n/n = 1$。可以证明，对于一个足够大的常数 $k_0$，负载至少为 $k_0$ 的箱子比例 $\beta_{k_0}(n)$ 是非常小的。具体来说，可以选择 $k_0$ 使得 w.h.p. $\beta_{k_0}(n）\le \frac{1}{2e}$
   2. **迭代：**假设 w.h.p. $\beta_{k-1}(n）\le \frac{1}{2e \cdot \delta^{d^{k-1-k_0}}}$ 对于 $k > k_0$ 成立，其中 $\delta$ 是一个稍大于 1 的常数
      从 $\beta_k \le 2 \beta_{k-1}^d$，我们有：
   $$
      \beta_k \lesssim (\beta_{k-1})^d \\
      \beta_{k_0+1} \lesssim \beta_{k_0}^d \\
      \beta_{k_0+2} \lesssim (\beta_{k_0+1})^d \lesssim (\beta_{k_0}^d)^d = \beta_{k_0}^{d^2}\\
      $$
      
   以此类推，
      
   $$
      \beta_{k_0+j} \lesssim \beta_{k_0}^{d^j}
      $$
      
   我们取 $\beta_{k_0} \le \epsilon_0 = 1/C'$，其中 $C'$ 是一个足够大的常数（如 $e$ 或 $2e$）。则 w.h.p. $N_{k_0+j}(n）= n \beta_{k_0+j} \lesssim n（\epsilon_0)^{d^j}$
   3. **确定最大高度：**我们想找到最小的 $k = k_0+j$ 使得 $N_k(n）< 1$。这要求：
   
   $$
      n (\epsilon_0)^{d^j} < 1 \\
      n < (\epsilon_0^{-1})^{d^j}
      $$
   
   两边取自然对数：
   
   $$
      \ln n < d^j \ln(\epsilon_0^{-1}) \\
      \ln(\ln n) < j \ln d + \ln(\ln(\epsilon_0^{-1}))
      $$
   
   因此，使得 $N_k(n）< 1$ 的最小整数 $j$ 大约为 $\frac{\ln \ln n}{\ln d} - O(1)$
      对应的负载 $k = k_0 + j$ 为：
   
   $$
      k = k_0 + \frac{\ln \ln n}{\ln d} - O(1) = \frac{\ln \ln n}{\ln d} + O(1)
      $$
   
   这意味着，当负载 $k$ 超过 $\frac{\ln \ln n}{\ln d} + C_0$（其中 $C_0$ 是某个足够大的常数）时，w.h.p. 不存在负载至少为 $k$ 的箱子
4. **处理 w.h.p.：**上述递归的每一步都以高概率成立。设第 $k$ 步失败的概率为 $p_k \le n^{-(c+1)}$。我们关心的最大高度 $k_{max} = \frac{\ln \ln n}{\ln d} + O(1)$ 是 $o(\log n)$。根据联合界（Union Bound），所有步骤（从 $k_0$ 到 $k_{max}$）都成功的概率至少是 $1 - \sum_{k=k_0}^{k_{max}} p_k \ge 1 - k_{max} n^{-(c+1)} = 1 - o(\log n）n^{-(c+1)} = 1 - o(n^{-c})$。因此，最终结论以高概率成立

**证毕。**



# 7 Cuckoo Hashing

## 7.1 使用 Pairwise Independent 哈希函数的问题

由于缺乏对三个或更多键的独立性保证，Cuckoo 图的结构可能偏离真随机图模型。一个关键的问题是**多个项哈希到相同位置对的概率可能显著高于预期**。

考虑一个具体的“坏”事件：存在三个不同的项 $x, y, z$ 被哈希到完全相同的两个位置，即 $h_1(x)=h_1(y)=h_1(z)=i$ 且 $h_2(x)=h_2(y)=h_2(z)=j$。或者写成 $h(x)=h(y)=h(z)=v$，其中 $v=(i,j)$。

如果发生这种情况，这三个项将永远争夺 $T_1[i]$ 和 $T_2[j]$ 这两个存储单元。只要这三个项中的任何一个被插入或被踢到这两个位置之一，就必然会立即踢出另一个竞争者，形成一个极短的循环（x -> y -> z -> x...)，导致插入操作在两次或三次踢出后就因循环而失败（或者很快达到 `MaxLoop`）。这使得包含这三个项中任何一个的插入操作几乎肯定会失败。

## 7.2 问题出现概率

设 $n$ 为要插入的项数，每个表的大小为 $r$（通常 $r =（1+\epsilon)n$）。组合哈希函数 $h=(h_1, h_2)$ 的值域大小为 $r^2$

* **真随机情况下的概率：**如果 $h$ 是真随机函数，对于特定的三个不同项 $x, y, z$ 和特定的位置对 $v=(i,j)$，事件 $h(x)=v \land h(y)=v \land h(z)=v$ 发生的概率是 $(1/r^2)^3 = 1/r^6$
* **Pairwise Independent 情况下的概率：**对于某些成对独立哈希函数族，三个不同项 $x, y, z$ 哈希到同一个值 $v$ 的概率 $P_3(v）= P(h(x)=v \land h(y)=v \land h(z)=v)$ 可能远大于 $1/r^6$。有研究表明，对于某些成对独立族，这个概率可能高达 $\Theta(1/r^3)$。这比真随机情况高了 $r^3$ 倍

现在我们计算整个哈希表中**至少发生一次**三项冲突的概率。设 $X_v$ 为哈希到特定位置对 $v=(i,j)$ 的项的数量。我们感兴趣的是事件 $E = \{\exists v \text{ s.t. } X_v \ge 3\}$ 的概率

我们可以通过计算“冲突三元组”的总期望数量来估计 $P(E)$。一个冲突三元组是指一个集合 $\{x, y, z\}$ 使得 $h(x)=h(y)=h(z)$。设 $Y = \sum_{v} \binom{X_v}{3}$ 为冲突三元组的总数

$$
\begin{aligned}
E[Y] & = E\left[\sum_v \binom{X_v}{3}\right] = \sum_v E\left[\binom{X_v}{3}\right] \\
E\left[\binom{X_v}{3}\right] & = \sum_{\{x,y,z\} \subset \{items\}, |\{x,y,z\}|=3} P(h(x)=v \land h(y)=v \land h(z)=v)
\end{aligned}
$$

假设对于任意不同的 $x, y, z$， $P(h(x)=v \land h(y)=v \land h(z)=v)$ 的上界为 $O(1/r^3)$（这代表了成对独立函数可能出现的最坏情况之一）

$$
\begin{aligned}
E[Y] &= \sum_v \binom{n}{3} \cdot O(1/r^3) \\
&= r^2 \cdot \binom{n}{3} \cdot O(1/r^3) \\
&= O\left(r^2 \cdot \frac{n^3}{6} \cdot \frac{1}{r^3}\right) \\
&= O\left(\frac{n^3}{r}\right)
\end{aligned}
$$

在 Cuckoo Hashing 中，为了保证较低的失败率，通常需要 $r = \Theta(n)$。代入 $r=\Theta(n)$：

$$
E[Y] = O\left(\frac{n^3}{n}\right) = O(n^2)
$$

这个结果意味着，使用可能存在问题的成对独立哈希函数时，我们**期望**在哈希表中看到 $O(n^2)$ 个“三项冲突”事件（即三个不同的项哈希到完全相同的两个位置）

由于 $E[Y] = O(n^2)$ 远大于 1（对于足够大的 $n$），根据马尔可夫不等式 $P(Y \ge 1）\le E[Y]$ 无法给出有意义的界限。然而，一个如此高的期望值强烈表明，**至少发生一次**三项冲突的概率 $P(Y \ge 1)$ 会非常高，对于大的 $n$ 会趋近于 1



# 8 Feistel Cipher

**1. 构造广义 Feistel 操作**

我们将原始操作中的位串域 $\{0,1\}^t$ 替换为整数域 $[n] = \{0, 1, ..., n-1\}$，并将异或操作 $\oplus$ 替换为模 $n$ 加法 $+_n$（为简洁起见，后续用 $+$ 表示模 $n$ 加法）

设 $f: [n] \to [n]$ 是从一个 k-universal 哈希函数族 $\mathcal{F}$ 中随机选择的哈希函数。我们定义广义 Feistel 操作 $G_f: [n] \times [n] \to [n] \times [n]$ 如下：
对于输入对 $(x_1, x_2）\in [n] \times [n]$，输出对 $(y_1, y_2)$ 为：

$$
(y_1, y_2) = G_f(x_1, x_2) = ((f(x_1) + x_2) \pmod n, x_2)
$$

我们主要关注 $x_1$ 到 $y_1$ 的映射，记作 $F_{f, x_2}(x_1）=（f(x_1）+ x_2）\pmod n$

**2. k-universality 定义**

一个哈希函数族 $\mathcal{H} = \{h: U \to V\}$ 被称为 **k-universal**，如果对于任意 $k$ 个不同的输入 $u_1, u_2, ..., u_k \in U$，以及任意 $k$ 个（不一定不同的）输出 $v_1, v_2, ..., v_k \in V$，当 $h$ 从 $\mathcal{H}$ 中均匀随机选取时，满足：

$$
P_{h \in \mathcal{H}}(h(u_1)=v_1 \land h(u_2)=v_2 \land \dots \land h(u_k)=v_k) = \frac{1}{|V|^k}
$$

**3. 证明广义 Feistel 操作的 k-universality**

我们需要证明，在题目给定的条件下，上述构造的映射 $x_1 \mapsto y_1$ 是 k-universal 的

* $f$ 是从 k-universal 哈希函数族 $\mathcal{F}: [n] \to [n]$ 中均匀随机选取的
* 考虑 $k$ 个输入对 $(x_{1,1}, x_{2,1}),（x_{1,2}, x_{2,2}), ...,（x_{1,k}, x_{2,k})$
* 输入 $x_{1,1}, x_{1,2}, ..., x_{1,k}$ 是 $[n]$ 中 $k$ 个互不相同的值
* 输入 $x_{2,1}, x_{2,2}, ..., x_{2,k}$ 是 $[n]$ 中 $k$ 个互不相同的值

证明对于任意 $k$ 个（不一定不同的）目标输出值 $Y_1, Y_2, ..., Y_k \in [n]$，以下概率成立：

$$
P_{f \in \mathcal{F}}(F_{f, x_{2,1}}(x_{1,1}) = Y_1 \land \dots \land F_{f, x_{2,k}}(x_{1,k}) = Y_k) = \frac{1}{n^k}
$$

考虑事件 $E = \{ F_{f, x_{2,1}}(x_{1,1}）= Y_1 \land \dots \land F_{f, x_{2,k}}(x_{1,k}）= Y_k \}$

根据 $F_{f, x_2}$ 的定义，事件 $E$ 等价于：

$$
E = \{ (f(x_{1,1}) + x_{2,1}) \equiv Y_1 \pmod n \land \dots \land (f(x_{1,k}) + x_{2,k}) \equiv Y_k \pmod n \}
$$

将每个等式中的 $x_{2,i}$ 移到右边（在模 $n$ 意义下做减法，即加上模 $n$ 的加法逆元）：

$$
E = \{ f(x_{1,1}) \equiv (Y_1 - x_{2,1}) \pmod n \land \dots \land f(x_{1,k}) \equiv (Y_k - x_{2,k}) \pmod n \}
$$

令 $Z_i =（Y_i - x_{2,i}）\pmod n$。则 $Z_1, Z_2, ..., Z_k$ 是 $[n]$ 中的 $k$ 个（不一定不同的）确定的值。事件 $E$ 可以写为：

$$
E = \{ f(x_{1,1}) = Z_1 \land f(x_{1,2}) = Z_2 \land \dots \land f(x_{1,k}) = Z_k \}
$$

我们已知：

1. 输入 $x_{1,1}, x_{1,2}, ..., x_{1,k}$ 是 $k$ 个互不相同的值。
2. $f$ 是从 k-universal 哈希函数族 $\mathcal{F}: [n] \to [n]$ 中均匀随机选取的。
3. $Z_1, Z_2, ..., Z_k$ 是 $k$ 个任意指定的目标输出值。

根据 k-universal 哈希函数族的定义，对于 $k$ 个不同的输入 $x_{1,1}, ..., x_{1,k}$ 和任意 $k$ 个目标输出 $Z_1, ..., Z_k$，当 $f$ 从 $\mathcal{F}$ 中随机选取时，有：

$$
P_{f \in \mathcal{F}}(f(x_{1,1}) = Z_1 \land f(x_{1,2}) = Z_2 \land \dots \land f(x_{1,k}) = Z_k) = \frac{1}{n^k}
$$

因此，我们所求的概率为：

$$
P(E) = P_{f \in \mathcal{F}}(F_{f, x_{2,1}}(x_{1,1}) = Y_1 \land \dots \land F_{f, x_{2,k}}(x_{1,k}) = Y_k) = \frac{1}{n^k}
$$

此结果表明，对于任意 $k$ 个不同的输入 $x_{1,1}, ..., x_{1,k}$ （以及任意对应的 $k$ 个不同的 $x_{2,1}, ..., x_{2,k}$），当 $f$ 从 k-universal 族中随机选择时，输出 $y_{1,1}, ..., y_{1,k}$ 同时等于任意指定的 $k$ 个目标值 $Y_1, ..., Y_k$ 的概率都是 $1/n^k$。这正是 k-universality 的定义

**4. 结论**

我们构造了广义 Feistel 操作 $(x_1, x_2）\mapsto（(f(x_1）+ x_2）\pmod n, x_2)$。当哈希函数 $f$ 从一个 k-universal 族 $\mathcal{F}: [n] \to [n]$ 中随机选择时，并且给定 $k$ 个输入对 $(x_{1,1}, x_{2,1}), ...,（x_{1,k}, x_{2,k})$ 满足 $x_{1,1}, ..., x_{1,k}$ 互不相同且 $x_{2,1}, ..., x_{2,k}$ 互不相同时，该操作将 $x_1$ 映射到 $y_1 =（f(x_1）+ x_2）\pmod n$ 的方式是 **k-universal** 的。即，对于任意 $k$ 个目标值 $Y_1, ..., Y_k \in [n]$， $P(y_{1,1}=Y_1 \land \dots \land y_{1,k}=Y_k）= 1/n^k$
