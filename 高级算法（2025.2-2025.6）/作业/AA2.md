# 1. Tabulation Hashing 的扩展

## Twisted Tabulation Hashing

Twisted tabulation 的核心思想是对最后一个字符进行“扭曲”处理。它不是直接用最后一个字符去查表，而是用这个字符与前面所有字符的异或结果再进行异或后去查表。

为什么我们需要这种“扭曲”？我认为原因在于 Simple tabulation 在处理某些分布时会表现出规律性。比如说，如果你有一堆键，它们的前几个字符都相同，只有最后一个字符不同，那么这些键的哈希值之间会存在一定的相关性。这种相关性在理论分析中是个大问题，因为它违背了我们对“足够随机”的期望。

Twisted tabulation 的巧妙之处在于它破坏了这种规律性。假设我们有一个键 $x = (x_1, x_2, ..., x_k)$，在 simple tabulation 中，哈希值是 $T_1[x_1] ⊕ T_2[x_2] ⊕ ... ⊕ T_k[x_k]$。而在 twisted tabulation 中，我们首先计算 $σ = T_1[x_1] ⊕ T_2[x_2] ⊕ ... ⊕ T_{k-1}[x_{k-1}]$，这个 $σ$ 就是“twister”，然后最终的哈希值就会变成 $σ ⊕ T_k[x_k ⊕ σ]$。

我们很容易注意到，最后一个表的查找不是直接用 $x_k$，而是用 $x_k ⊕ σ$。当前面的字符相同时，$σ$ 值也相同，但是 $x_k ⊕ σ$ 这个操作让不同的 $x_k$ 值在表中能访问完全不同的位置，从而打破了原有的相关性。

这种设计使 Twisted tabulation 提供了更强的分布性质。也就是说，在概率分析中，我们可以得到更紧的误差界限。同时，它在 min-wise hashing 中的偏差也非常小，这一点对于近似算法来说很重要。

我认为 Twisted Tabulation Hashing 这个方法的优点在于，它几乎保持了 simple tabulation 的所有优势——实现简单，速度快，内存访问模式好。但它的缺点在于它的理论分析更复杂，而且它在某些极端情况下可能还是不够随机。

## Mixed Tabulation Hashing

Mixed Tabulation Hashing 是由 Dahlgaard 和 Thorup 提出的，它可以看作是对“Double Tabulation”的异或操作。

Double tabulation 的思想是先用 simple tabulation 得到一个中间结果，然后把这个中间结果再作为新的键进行第二次 simple tabulation。这样做的好处是可以获得更高的独立性，理论上可以达到接近完全随机的效果。

而 Mixed Tabulation 会同时进行两个独立的 tabulation 过程。第一个过程是标准的 simple tabulation，第二个过程是 double tabulation。然后算法将得到的两个结果进行异或，最终得到一个哈希值。通过混合两种不同强度的随机化过程，我们可以同时获得 simple tabulation 的速度优势和 double tabulation 的理论保证。因为如果 simple tabulation 在某些输入模式下表现不佳，double tabulation 可能会表现良好，反之亦然。两者的异或结果往往能够避免任何一种方法单独的弱点。

但是我认为这种实现方式也是有局限性的，实现 mixed tabulation 需要维护更多的查找表，也就需要进行更多的内存访问，这个设计在实际应用中会明显拖慢运行速度。并且，由于我们需要访问的内存位置更多且更分散，cache 的行为会变得更难预测。

从理论的角度来看，mixed tabulation 的优势主要体现在一些非常特殊的场景中。比如在某些高维空间的近似算法中，或者在处理具有特殊结构的数据时，它可能会表现得比 simple 或 twisted tabulation 更好。但对于大多数常见的应用场景，这种额外的复杂度可能并不值得。



# 2. Johnson-Lindenstrauss变换

对于满足 $|\mathbf{x}|_\infty \leq \alpha$ 的单位向量 $\mathbf{x} \in \mathbb{S}^{d-1}$，Johnson-Lindenstrauss 变换可以降维到：
$$
k = O\left(\frac{\log(1/\delta)}{\epsilon^2 \alpha^2 d}\right)
$$
**证明**：

考虑标准的随机投影矩阵 $\mathbf{R} \in \mathbb{R}^{k \times d}$，其中 $R_{ij} \sim \mathcal{N}(0, 1/k)$

对于 $\mathbf{y} = \mathbf{R}\mathbf{x}$，我们有：
$$
\begin{align*}
\mathbb{E}[|\mathbf{y}|_2^2] &= |\mathbf{x}|_2^2 = 1 \\
\text{Var}(|\mathbf{y}|*2^2) &= \text{Var}\left(\sum*{i=1}^k y_i^2\right) = \sum_{i=1}^k \text{Var}(y_i^2)
\end{align*}
$$
对于每个 $y_i = \sum_{j=1}^d R_{ij}x_j$，我们有： 
$$
\text{Var}(y_i^2) = 2\left(\sum_{j=1}^d \frac{x_j^2}{k}\right)^2 \leq 2\left(\sum_{j=1}^d \frac{\alpha^2}{k}\right)^2 = \frac{2\alpha^4 d^2}{k^2}
$$
因此：
$$
\text{Var}(|\mathbf{y}|_2^2) \leq k \cdot \frac{2\alpha^4 d^2}{k^2} = \frac{2\alpha^4 d^2}{k}
$$
为了保证：
$$
\mathbb{P}[||\mathbf{y}|_2^2 - 1| > \epsilon] \leq \delta
$$
我们需要：
$$
\frac{2\alpha^4 d^2}{k} \leq \frac{\epsilon^2}{\log(1/\delta)}
$$
所以：
$$
\frac{2\alpha^4 d^2}{k} \leq \frac{\epsilon^2}{\log(1/\delta)}
$$
对于单位向量，我们有 $\sum_{i=1}^d x_i^2 = 1$，且 $|x_i| \leq \alpha$。

当 $\alpha \leq 1/\sqrt{d}$ 时，向量的"有效维数"大约为 $1/\alpha^2$ 而不是 $d$

使用 Bernstein 型不等式可以得到：
$$
k = O\left(\frac{\log(1/\delta)}{\epsilon^2 \alpha^2 d}\right)
$$


# 3. 为不同的度量空间设计合适的 LSH 函数

## 3.1 环形空间的 LSH 设计

在这个空间中，每个维度都是“环形”的，距离定义为： $d(x,y) = \sqrt{\sum_i (\min{|x_i-y_i|, n-|x_i-y_i|})^2}$。这意味着在每个维度上，点 0 和点 n 是相邻的

我们可以将每个坐标 $x_i \in [0,n]$ 映射到单位圆上： $z_i = e^{2\pi i x_i/n}$

然后使用：$h(x) = \text{sign}\left(\text{Re}\left(\sum_{i=1}^d w_i z_i\right)\right)$，其中 $w_i$ 是随机复数权重

**理由**：这种设计产生的随机集合在每个维度上都是“弧形区间”，能够很好地适应环形拓扑

## 3.2 球面空间的 LSH 设计

球面上的距离是角距离：$d(x,y) = \arccos⟨x,y⟩$。相近的点有较大的内积，较远的点有较小的内积

我们使用随机超平面哈希来设计：$h(x) = \text{sign}(⟨w, x⟩)$，其中 $w \sim \mathcal{N}(0, I_d)$ 是随机单位向量

**理由**：对于两个单位向量$x, y$，它们被哈希到同一个桶的概率是：$P[h(x) = h(y)] = 1 - \frac{\theta}{\pi}$，其中 $\theta = \arccos⟨x,y⟩$ 是它们之间的角度

随机超平面将球面分成两个半球，这种分割可以保证：①角度相近的点更可能在同一侧；②哈希函数对球面上的旋转具有不变性



# 4. 证明任意确定的列稀疏度为 1 的矩阵 $\Pi$ 作为 OSE 都需要 $m = \Omega(d^2)$

假设存在某个确定的 $\Pi$ 使得 $m = o(d^2)$

由于 $\Pi$ 每列只有一个非零元素，我们可以将 $\Pi$ 表示为：$\Pi = \text{diag}(\alpha_1, \alpha_2, \ldots, \alpha_m) \cdot P$

其中 $P \in \{0,1\}^{m \times n}$ 是一个置换类型的矩阵（每列恰好有一个1），$\alpha_i$ 是非零标量

不失一般性，可以假设所有 $\alpha_i = 1$

考虑标准基向量 $e_i, e_j \in \mathbb{R}^d$（$i \neq j$）

对于随机矩阵 $A$：
- $Ae_i$ 是 $A$ 的第 $i$ 列，是一个随机的标准基向量
- $A(e_i + e_j)$ 是 $A$ 的第 $i$ 列和第 $j$ 列的和

设 $A$ 的第 $i$ 列在位置 $k_i$ 处为 1，第 $j$ 列在位置 $k_j$ 处为 1

那么：
- $\|Ae_i\|_2 = 1$
- $\|A(e_i + e_j)\|_2 = \sqrt{2}$（当 $k_i \neq k_j$ 时）
- $\|A(e_i + e_j)\|_2 = 1$（当 $k_i = k_j$ 时）

现在考虑 $\Pi Ae_i$ 和 $\Pi A(e_i + e_j)$：

由于 $\Pi$ 每列只有一个非零元素，$\Pi Ae_i$ 要么是零向量，要么是某个标准基向量

考虑所有 $d$ 个向量 $\Pi Ae_1, \Pi Ae_2, \ldots, \Pi Ae_d$

每个 $\Pi Ae_i$ 对应一个"球"被投入到 $m$ 个"桶"中的某一个

根据 Birthday Paradox，如果 $m = o(d^2)$，那么以 $\Omega(1)$ 的概率，存在 $i \neq j$ 使得 $\Pi Ae_i$ 和 $\Pi Ae_j$ 落在同一个位置

假设 $\Pi Ae_i$ 和 $\Pi Ae_j$ 都在位置 $\ell$ 处为1

考虑向量 $e_i - e_j$：
- $\|A(e_i - e_j)\|_2 = \sqrt{2}$（当对应的 $A$ 的列不重合时）
- 但是 $\|\Pi A(e_i - e_j)\|_2 = 0$（因为 $\Pi Ae_i = \Pi Ae_j$）

这意味着 $\Pi$ 将一个非零向量 $A(e_i - e_j)$ 映射到了零向量，违反了子空间嵌入的要求

为了使 $\Pi$ 成为一个有效的子空间嵌入，我们需要对所有 $v \in \text{span}(A)$ 都有：$(1-\epsilon)\|v\|_2 \leq \|\Pi v\|_2 \leq (1+\epsilon)\|v\|_2$

但是当 $m = o(d^2)$ 时，根据 Birthday Paradox，以 $\Omega(1)$ 的概率存在 $i \neq j$ 使得 $\Pi Ae_i$ 和 $\Pi Ae_j$ 在同一个坐标上有支集重叠

这会导致某些线性组合 $\alpha e_i + \beta e_j$ 被映射后的范数显著偏离原来的范数，破坏子空间嵌入性质

因此，为了保证 $\Pi$ 能够作为随机矩阵 $A$ 的列空间的有效子空间嵌入，我们必须有 $m = \Omega(d^2)$



# 5. 网络流图算法设计

**基本思想**：将有容量限制的节点分裂成两个节点，用一条有限容量的边连接它们

**具体步骤**：

1. 对于每个有容量限制的节点 $v$（除了源点 $s$ 和汇点 $t$），创建两个新节点：
   - 入节点 $v_{in}$：接收所有原来指向 $v$ 的边
   - 出节点 $v_{out}$：发出所有原来从 $v$ 出发的边

2. 在 $v_{in}$ 和 $v_{out}$ 之间添加一条容量为 $c_v$ 的边

3. 重新连接边：
   - 原图中的边 $(u,v)$ 变成 $(u_{out}, v_{in})$
   - 边的容量设为无穷大（或足够大的数）

4. 在新图上运行标准的最大流算法（如Ford-Fulkerson或Dinic算法）

**算法正确性证明**：

- **引理1**：新图中的任何可行流对应原图中的一个可行流，反之亦然

  证明：
  - 设 $f'$ 是新图中的可行流，定义原图中的流 $f$ 如下：对于原图中的边 $(u,v)$，令 $f(u,v) = f'(u_{out}, v_{in})$

  - 流量守恒：在新图中，对于节点 $v_{in}$ 和 $v_{out}$，由于它们之间只有一条边，流入 $v_{in}$ 的流量等于从 $v_{out}$ 流出的流量，这保证了原图中节点 $v$ 的流量守恒

  - 容量约束：原图中通过节点 $v$ 的流量等于新图中边 $(v_{in}, v_{out})$ 的流量，由于该边容量为 $c_v$，所以原图的节点容量约束得到满足

- **引理2**：两个图的最大流值相等

  证明：
  - 由引理 1，新图的任何流值为 $F$ 的可行流对应原图中流值为 $F$ 的可行流
  - 反过来也成立
  - 因此两个图的最大流值必须相等



# 6. 最小化总采购成本

设 $x_{ij}$ 表示从供应商 $i$ 采购零件 $j$ 的数量，我们的目标函数是：$\min \sum_{i=1}^{m} \sum_{j=1}^{k} a_{ij} x_{ij}$

并且存在以下几个约束条件：

- $\sum_{i=1}^{m} x_{ij} = d_j, \forall j \in [k]$
- $x_{ij} \leq c_{ij}, \forall i \in [m], j \in [k]$
- $x_{ij} \geq 0, \forall i, j$

**我们使用最小费用最大流算法求解**：

- 步骤 1：检查是否 $\sum_{i=1}^{m} c_{ij} \geq d_j$ 对所有 $j \in [k]$ 成立。如果不成立，问题无解

- 步骤 2：构造网络流图 $G = (V, E)$：

  - 节点集合 $V$：
    - 源点 $s$
    - 供应商节点 $u_i$（对应供应商 $i$，$i \in [m]$）
    - 零件节点 $v_j$（对应零件 $j$，$j \in [k]$）
    - 汇点 $t$

  - 边集合 $E$ 及其容量和费用：
    - 边 $(s, u_i)$：容量 $\sum_{j=1}^{k} c_{ij}$，费用 $0$
    - 边 $(u_i, v_j)$：容量 $c_{ij}$，费用 $a_{ij}$
    - 边 $(v_j, t)$：容量 $d_j$，费用 $0$

  - 流量需求：我们需要从 $s$ 到 $t$ 推送流量 $\sum_{j=1}^{k} d_j$

- 步骤 3：运行 Successive Shortest Path 算法

- 步骤 4：设最优流为 $f$，则原问题的最优解为：$x_{ij}^* = f(u_i, v_j)$

**正确性证明**：

- **充分性**：设 $f$ 是构造图上的可行流，流量为 $F = \sum_{j=1}^{k} d_j$

  定义 $x_{ij} = f(u_i, v_j)$，我们验证这是原问题的可行解：

  - 非负性：由于 $f$ 是可行流，$f(u_i, v_j) \geq 0$，所以 $x_{ij} \geq 0$


  - 由于边 $(u_i, v_j)$ 的容量为 $c_{ij}$，有 $f(u_i, v_j) \leq c_{ij}$，即 $x_{ij} \leq c_{ij}$


  - 对于零件 $j$，由流量守恒：$\sum_{i=1}^{m} f(u_i, v_j) = f(v_j, t) = d_j$，即 $\sum_{i=1}^{m} x_{ij} = d_j$


- **必要性**：设 $x^*$ 是原问题的可行解。定义流 $f$ 如下：

  - $f(s, u_i) = \sum_{j=1}^{k} x_{ij}^*$

  - $f(u_i, v_j) = x_{ij}^*$  

  - $f(v_j, t) = d_j$


可以验证这是一个可行流，流量为 $\sum_{j=1}^{k} d_j$。



# 7. 二分图最大匹配和最小顶点覆盖问题

**最大匹配的线性规划**：设 $x_e$ 表示边 $e$ 是否在匹配中（$x_e = 1$ 表示选中，$x_e = 0$ 表示未选中）
$$
\max \sum_{e \in E} x_e
$$
约束条件：
$$
\sum_{e \text{ incident to } v} x_e \leq 1, \quad \forall v \in L \cup R, \quad x_e \geq 0, \quad \forall e \in E
$$
**最小顶点覆盖的线性规划**：设 $y_v$ 表示顶点 $v$ 是否在顶点覆盖中
$$
\min \sum_{v \in L \cup R} y_v
$$
约束条件：
$$
y_u + y_v \geq 1, \quad \forall (u,v) \in E, \quad y_v \geq 0, \quad \forall v \in L \cup R
$$
**证明**：在二分图中，最大匹配的大小等于最小顶点覆盖的大小

**引理1**：最大匹配的大小 ≤ 最小顶点覆盖的大小
设 $M$ 是最大匹配，$C$ 是最小顶点覆盖。对于 $M$ 中的每条边 $(u,v)$，由于 $C$ 是顶点覆盖，必须有 $u \in C$ 或 $v \in C$（或者两者都在）。由于 $M$ 是匹配，$M$ 中的边两两不相邻，所以我们需要至少 $|M|$ 个顶点来覆盖 $M$ 中的所有边。因此 $|M| \leq |C|$

**引理2**：存在大小相等的匹配和顶点覆盖

设 $M$ 是一个最大匹配。定义：
- $U$ = $L$ 中所有未匹配的顶点
- 从 $U$ 开始，用BFS/DFS找到所有可以通过"未匹配边→匹配边→未匹配边→..."这样的交替路径能到达的顶点
- 设这些可到达的顶点集合为 $S$

定义顶点覆盖 $C$：
$$
C = (L \setminus S) \cup (R \cap S)
$$
我们需要证明：
1. $C$ 是一个顶点覆盖
2. $|C| = |M|$

**证明 $C$ 是顶点覆盖**：考虑任意边 $(u,v)$，其中 $u \in L, v \in R$。我们需要证明 $u \in C$ 或 $v \in C$

情况1：如果 $u \notin S$，那么 $u \in L \setminus S \subseteq C$。

情况2：如果 $u \in S$，我们声称 $v \in S$。为什么？因为如果 $u \in S$ 且 $u$ 是从 $U$ 开始通过交替路径到达的，那么这条路径必须以匹配边结束于 $u$（除非 $u \in U$）。现在考虑边 $(u,v)$：
- 如果 $(u,v)$ 是匹配中的边，那么 $v$ 必须在到达 $u$ 的路径上，所以 $v \in S$
- 如果 $(u,v)$ 不是匹配中的边，那么我们可以通过这条未匹配边从 $u$ 到达 $v$，所以 $v \in S$

因此如果 $u \in S$，必有 $v \in S$，从而 $v \in R \cap S \subseteq C$

**证明 $|C| = |M|$**：观察到 $S \cap L$ 中的每个顶点都被匹配到 $S \cap R$ 中的某个顶点（除了 $U$ 中的顶点）。而 $U \subseteq S \cap L$

通过仔细分析交替路径的结构，可以证明：
$$
|L \setminus S| + |R \cap S| = |M|
$$
因此 $|C| = |M|$

结合引理 1 和引理 2，我们得到最大匹配的大小等于最小顶点覆盖的大小



# 8. 0-1 背包问题

**决策变量**：$x_i \in \{0,1\}$ 表示第 $i$ 个物品是否被选中

**整数规划**：
$$
\max \sum_{i=1}^{n} v_i x_i
$$
约束条件：
$$
\sum_{i=1}^{n} w_i x_i \leq C, \quad x_i \in \{0,1\}, \quad i = 1,2,\ldots,n
$$
**线性规划松弛**：
$$
\max \sum_{i=1}^{n} v_i x_i
$$
约束条件：
$$
\sum_{i=1}^{n} w_i x_i \leq C, \quad, 0 \leq x_i \leq 1, \quad i = 1,2,\ldots,n
$$
基于线性规划松弛最优解的结构，我设计以下取整方案：

1. 求解线性规划松弛，得到最优解 $x^*$
2. 找到临界位置 $k$，使得 $x_k^* \in (0,1)$
3. 构造两个候选解：
   - 解1：$x_i = 1$ 对 $i = 1,\ldots,k-1$，其他为0（向下取整）
   - 解2：$x_i = 1$ 对 $i = 1,\ldots,k$，其他为0（如果重量允许）
4. 选择价值更大且可行的解

**算法描述**：
```
1. 按 v_i/w_i 降序排列物品
2. 求解线性规划松弛，得到最优解 x* 和最优值 OPT_LP
3. 找到 k 使得 sum(w_i, i=1 to k-1) ≤ C < sum(w_i, i=1 to k)
4. 构造解1：选择物品 1,2,...,k-1，价值为 V1 = sum(v_i, i=1 to k-1)
5. 如果 sum(w_i, i=1 to k) ≤ C，构造解2：选择物品 1,2,...,k，价值为 V2 = sum(v_i, i=1 to k)
6. 返回 max{V1, V2}
```

**近似比分析**：设线性规划松弛的最优值为 $OPT_{LP}$，整数规划的最优值为 $OPT_{IP}$。显然 $OPT_{LP} \geq OPT_{IP}$。

根据线性规划最优解的结构：
$$
OPT_{LP} = \sum_{i=1}^{k-1} v_i + x_k^* v_k
$$
其中 $x_k^* = \frac{C - \sum_{i=1}^{k-1} w_i}{w_k}$

我们的算法返回 $\max\{V_1, V_2\}$，其中：
- $V_1 = \sum_{i=1}^{k-1} v_i$
- $V_2 = \sum_{i=1}^{k} v_i$（如果可行）

**情况1**：如果 $w_k > C$（即解2不可行），此时我们只有解1，$V_1 = \sum_{i=1}^{k-1} v_i$。由于 $w_k > C$，没有任何可行解能包含物品 $k$，所以整数规划的最优解也只能从前 $k-1$ 个物品中选择。而解1是这种情况下的最优解，所以算法是最优的

**情况2**：如果 $w_k \leq C$（即解2可行）。我们需要证明 $\max\{V_1, V_2\} \geq \frac{1}{2} OPT_{LP}$

因为 $OPT_{LP} = \sum_{i=1}^{k-1} v_i + x_k^* v_k \leq \sum_{i=1}^{k-1} v_i + v_k$

所以：
$$
V_1 = \sum_{i=1}^{k-1} v_i \geq OPT_{LP} - v_k
$$
另外，$V_2 = \sum_{i=1}^{k} v_i = V_1 + v_k$

如果 $V_1 \geq \frac{1}{2} OPT_{LP}$，我们就完成了

如果 $V_1 < \frac{1}{2} OPT_{LP}$，那么：
$$
v_k > OPT_{LP} - V_1 > OPT_{LP} - \frac{1}{2} OPT_{LP} = \frac{1}{2} OPT_{LP}
$$
因此 $V_2 = V_1 + v_k > \frac{1}{2} OPT_{LP}$

所以在任何情况下，$\max\{V_1, V_2\} \geq \frac{1}{2} OPT_{LP} \geq \frac{1}{2} OPT_{IP}$

因此我们的算法近似比为 2

