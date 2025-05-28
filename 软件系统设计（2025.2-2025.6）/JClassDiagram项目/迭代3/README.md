# 迭代三：多文件支持与命令行工具设计

> [!TIP]
>
> **开始之前**
>
> 为了能更好地改进软件系统设计的实验作业，我们诚挚地邀请您花1~2分钟时间来匿名填写一下 [迭代二调查问卷](https://www.wjx.cn/vm/rX581K6.aspx#) 。当然，这不是必须的。
>
> 【4.11更新】由于助教的失误，lab3循环依赖测试用例中出现了多个环的情况，助教正在紧急修复中(大哭)。
>
> 【4.11/4.17更新】测试用例已被修复，对于多个存在多个环的用例，输出任意一个环均被视为正确。4.17之前下载测试用例的小组可以重新下载。
>
> 【4.16/4.17更新】勘误：CommandLineTest.java 第503行改为：
>
> ```
> assertTrue(result.equals("Farmer <-- RedDuck\nRedDuck <.. Farmer\n") || result.equals("RedDuck <.. Farmer\nFarmer <-- RedDuck\n"), "relate Farmer RedDuck error");
> ```
>
> MixTest.java 第 125 - 131 行改为：
>
> ```
> String expecttCell2 = """
>         abstract class Cell {
>             # game: Game
>             + {abstract} meet(player: Player): void
>             + setGame(game: Game): void
>         }
>         """;
> ```
>
> 所有更新均已同步到gitlab仓管和GradeScope测试用例。给大家带来麻烦了，非常抱歉。

## 截止时间

**2025 年 4 月 30 日 23：59**

## 实验描述

在迭代二中，我们实现了更多类图元素的解析，绘制了包含关联和依赖关系的类图。除此之外，我们还实现了几个基于类图的程序分析器，帮助程序员发现代码中的“坏味道”。在迭代三中，我们将继续扩展JClassDiagram的功能，让它更实用、更好用。

> [!NOTE]
>
> **获取测试用例**
>
> 我们在 [GitLab 仓库](https://git.nju.edu.cn/Nboxff/software_design_framework_2025) 中添加了迭代三的测试用例，请将仓库的测试部分添加到你们小组的实验仓库中。
>
> 和迭代二的代码框架相比，迭代三的代码框架中多了`src/main/java/command`目录，其中包含了`CommandLineTool`类。此外，我们还准备了`JClassDiagram.java`文件，方便你使用交互式命令行进行测试。

## Part 1: 多文件支持

为了应对更加普遍的java项目，你需要使JClassDiagram能够解析多文件的java项目。

你需要对`ClassDiagramGenerator`类的接口进行升级，使其能够解析多文件的java项目。

**ClassDiagramGenerator.java**

```
public ClassDiagram parse(Path sourcePath) throws IOException {
    // TODO: finish me
}
```

- 当`sourcePath`指向.java文件时，你只需要解析该单文件
- 当`sourcePath`指向目录时，你需要递归遍历该目录下的所有java文件并创建`ClassDiagram`

> 在本实验中，为了简化起见，我们保证目录下不存在同名文件以及同名类



## Part 2: 命令行工具设计与实现

在Part 2中，我们将把JClassDiagram升级为交互式命令行工具，支持动态修改类图结构、查询程序分析器分析结果，并提供操作回退功能。

### 2.1 类图管理

你需要支持下面的命令，接口规范见2.4：

**类/接口/枚举类操作**

```
# 创建新类（支持抽象类标记）
add -c <name> [--abstract]
# 创建新接口 
add -i <name>
# 创建新枚举 (支持初始化枚举常量）,常量列表形如 RED,GREEN,BLUE 
# 枚举常量之间用逗号隔开，枚举常量不带有常量值
add -e <name> [--values=<constant list>]

# 删除指定类（自动级联删除类相关关系） 
delete -c <name>
# 删除指定接口（自动级联删除接口相关关系）
delete -i <name>
# 删除指定枚举（自动级联删除枚举类相关关系）
delete -e <name>
```

**字段操作**

为了减少逻辑类似的代码工作量，target-name只需要考虑**类名**，无需考虑接口和枚举。

对于修改操作，`--static`表示将字段修改为静态字段，若没有指定，则默认修改为非静态字段，与原先的字段无关。

```
# 添加新的字段(不考虑重名）/ modifier直接输入符号 / 默认修饰符为-(private)
add field <target-name> -n <field-name> -t <type> [--access=<modifier>] [--static]
# 删除字段
delete field <target-name> -n <field-name>
# 修改指定字段/默认修饰符为-(private)
modify field <target-name> -n <field-name> [--new-name=<name>] [--new-type=<type>] [--new-access=<modifer>] [--static]
```

> 对于字段的操作也有可能会改变类间关系😟不过为了更简单的实现，本次实验你不需要考虑这一点😊
>
> 话又说回来了，不能自动更新类间关系，那为什么不手动修改PlantUML文本呢？~~（手动狗头）~~

**方法操作**

- 为了减少逻辑类似的代码工作量，target-name只需要考虑**类名**，无需考虑接口和枚举。
- 对于修改操作，`--static`表示将字段修改为静态方法，若没有指定，则默认修改为非静态方法，与原先的字段无关。同理，`--abstract`表示将字段修改为抽象方法，若没有指定，则默认修改为非抽象方法。
- 和字段操作相同，默认修饰符为-(private)

```
# 添加新的方法, params形如参数名1:参数类型1,参数名2:参数类型2, ...
add function <target-name> -n <function-name> -t <ret-type> [--params=<params>] [--access=<modifier>] [--static] [--abstract]
# 删除方法
delete function <target-name> -n <function-name>
# 修改指定方法
modify function <target-name> -n <function-name> [--new-name=<name>] [--new-params=<params>] [--new-return=<ret-type>] [--new-access=<modifier>] [--static] [--abstract]
```

> 与字段操作相同，你也无需考虑对于方法的操作可能会改变类间关系。此外，你也无需考虑重名方法的情况。（当然考虑了更好）

**回退操作**

对于每一次的修改（包括创建、删除、修改），我们可以使用undo命令撤销修改。注意连续的undo操作可以撤回多次修改。

```
# 回退到上一次操作
undo
```

如果没有可以撤销的修改，则返回`"No command to undo"`。

> [!NOTE]
>
> **编程提示**
>
> 如果你想使用栈数据结构来实现回退功能，我们建议你使用`Deque`而不是`Stack`。
>
> (DeepSeek-R1) 虽然 `Stack` 可以实例化，但 Java 官方文档建议优先使用 `Deque` 接口的实现类（如 `ArrayDeque`）来实现栈的功能。例如：
>
> ```
> Deque<Integer> stack = new ArrayDeque<>();
> ```
>
> `Stack` 继承自 `Vector` 类（线程安全但性能较差），而 `Deque` 的实现类在非线程安全场景下性能更好。

### 2.2 类图查询

- 普通类/枚举类/接口的结构按照前两次迭代的 PlantUML文本 结构输出即可，结尾有一个换行符。
- 关系符号按照前两次迭代的格式输出，注意方向始终一致，与A, B两类的先后顺序无关。
- 对关系输出的先后关系不做特别要求，每一个关系占一行，中间不要有多余的换行符。

**基础元素查询**

- `hide`参数可以取值为`field/method/constant`, 分别隐藏字段/方法/枚举常量。
- 请严格按照迭代一、迭代二的格式返回，可以参考测试用例中的格式。

```
#查询普通类结构
query -c <name> [--hide=<field|method>]
#查询接口结构
query -i <name> [--hide=<method>]
#查询枚举类结构
query -e <name> [--hide=<constant|field|method>]
```

**指定关系查询**

- `element` 取值为类名/接口名/枚举类名。
- 返回的字符串应当有一个`\n`换行符。如果有多个关系，请用`\n`隔开。

```
# 查询"类"间关系
relate <elementA> <elementB>
```

### 2.3 查看分析结果

`elementName`取值为类名/接口名/枚举类名。

```
# 查询特定某个元素的BadSmell
smell detail <elementName>
```

- 在判定具体smell时牵涉到了某个元素，就认为这个元素上能嗅到bad smell。
- 比如过深继承树的每一环在被单独查询时都能够查出涉及过深继承。
- 输出格式同样维持迭代二的输出格式即可。
- 不用考虑Part 3部分的设计模式检测，即使是你完成Part 3以后。

### 2.4 输入与测试

本次实验的 Part 2 你需要实现自己的命令行解析与执行器，并在`CommandLineTool`的`execute`方法中调用自主实现的解析与执行器，实现以上命令行对应的效果。

**CommandLineTool.java**

```java
package command;

import diagram.ClassDiagram;

public class CommandLineTool {
    private ClassDiagram diagram;

    public CommandLineTool(ClassDiagram diagram) {
        this.diagram = diagram;
    }

    /**
     * @param command 输入的命令
     * @return 如果是查询性质语句，将查询的结果保存在返回值中。Undo语句可能返回的信息也保存在返回值中。
     */
    public String execute(String command) {
        // TODO: finish me
        return "";
    }
}
```



## Part 3: 简单设计模式检测 & 可配置的分析器

### 3.1 设计模式分析

在 3.1 中，你需要识别两种经典的设计模式：单例模式和策略模式。为了简化代码实现，我们对单例模式和策略模式的要求做了一定的限制。

**单例模式**

报告类A为可能的单例模式，当且仅当：

1. 不存在子类继承自类A
2. 没有公共（public）构造函数且存在私有构造函数
3. 包含静态私有字段，类型为自身类（例如 `private static A instance;`，字段名不作要求）
4. 提供静态公有方法获取实例（例如 `public static A getInstance()`，方法名不作要求）

你需要在`getCodeSmell()`方法中报告：`Possible Design Patterns: Singleton Pattern`

> 此时`getCodeSmell()`方法返回的不再是程序中的“坏味道”了，而是一系列分析器的分析结果。

**策略模式**

报告类图中可能存在策略模式，需要满足以下条件：

| **组件类型**   | **必须条件**                                                 |
| :------------- | :----------------------------------------------------------- |
| **策略接口**   | 接口或抽象类声明至少一个方法类名/接口名以`Strategy`、`Policy`或`Behavior`结尾 |
| **具体策略类** | 实现策略接口的多个具体类（≥2个）                             |
| **上下文类**   | 存在和策略接口的关联关系                                     |

你需要报告：`Possible Design Patterns: Strategy Pattern`

注意，实际设计模式判断比上述规则略复杂一些，为了实现简单，我们不考虑上下文类是否包含委托调用策略方法的逻辑以及上下文类如何设置策略。

### 3.2 可配置的分析器

有的时候，我们并不要想让所有的分析器工作，因为这会使分析报告非常冗长而其中很多信息并不是我们想要的。在 3.2 中，你需要实现一个可配置的分析器，使JClassDiagram能够根据配置文件中的规则对类图进行分析。

**若没有提供配置文件，你需要默认所有分析器均被开启**（这可能与现实中的项目不太一致，主要是为了你的程序能通过迭代二和3.1中的测试）。

XML配置文件的示例如下：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
    <analyzers>
        <analyzer>CircularDependencyAnalyzer</analyzer>
        <analyzer>ClassAnalyzer</analyzer>
    </analyzers>
</configuration>
```

在上面的例子中，我们只启用了`CircularDependencyAnalyzer`和`ClassAnalyzer`两个分析器，即循环依赖分析和类分析。这时即使有其他“坏味道”或有设计模式被发现，JClassDiagram也不会报告。

在配置文件中，所有分析器在配置文件中的标准命名如下：

- 类分析：`ClassAnalyzer`
- 继承树分析：`InheritanceTreeAnalyzer`
- 循环依赖分析：`CircularDependencyAnalyzer`
- 设计模式分析：`DesignPatternAnalyzer`

你需要为ClassDiagram类实现一个`loadConfig(configFile: String)`的公共方法，该方法接受一个字符串参数，表示配置文件的路径。 虽然这是我们实验的最后一个任务，但你仍然需要考虑**弹性的设计**。你需要考虑使用JSON格式、YAML格式的配置文件的情况，但为了减轻工作量，你不用具体实现它们。

到这里，软件系统设计-设计模式的作业就结束了，我们的JClassDiagram也基本完成了。希望大家在这次作业中有所收获，能够加深对面向对象设计的理解，也欢迎大家在问卷中对课程实验提出宝贵的建议。提前祝大家期末考试顺利！~~（明明才期中）~~

## 代码与测试

项目使用 jdk17 和 Maven 构建，你可以在 IDEA 或 VSCode 中打开项目，并使用Maven插件运行测试用例。为了便于测试，我们要求所有的项目Java代码都放在 `src/main/java` 目录下。

> [!TIP]
>
> **与GradeScope测试一致**
>
> 在迭代一中，有小组反馈自己本地的maven插件运行结果和OJ测试结果不一致，这可能和JVM的机制有关。OJ使用 `mvn test` 命令进行测试，我们建议你在本地同样使用这个命令进行测试。

我们提供了一些公开测试用例，保存在 `src/test/java` 目录下，你可以直接运行测试用例来检查你的实现是否正确。公开测试用例与 GradeScope上的用例一致，但GradeScope上会包含更多的测试用例。

**彩蛋**：我们选取了同学们大一到大三可能写过的代码为主题，使用AI生成了测试用例。我们鼓励大家使用AI工具生成测试用例来测试你的代码。

## 提交与评分

你需要提交 **项目代码** 和 **软件设计文档** 。

为了避免不必要的内卷，我们在框架代码中为你准备了设计文档模板（markdown格式）。你也可以从[这里](https://git.nju.edu.cn/Nboxff/software_design_framework_2025/-/raw/main/docs/iter3_template.md?ref_type=heads&inline=false)下载。

我们对迭代三的设计文档模板做出了调整，请不要直接使用迭代一或迭代二的文档模板。

**在 GradeScope 上提交项目代码：**

- 组队：在每次作业提交后，根据 GradeScope 的提示完成组队。
- 代码要求：
  - 代码必须位于 `src/main/java/` 目录下。
  - 使用框架代码的打包脚本打包并提交ZIP文件（不要提交.class文件，也不要提交测试文件），参考 lab0 的提交格式。
  - 提交：任一成员提交后，所有组员将看到分数。
  - 查看反馈：提交后30分钟内查看自动评分结果。

软件设计文档提交到**教学立方**的作业中。

## 其他要求与提示

### 使用Git管理小组软件项目

1. 你需要使用git来管理你小组的代码，请在开发的过程中遵守git的提交规范，我们会通过git提交记录跟踪你们的设计过程，并且将每次迭代的最终版本的git提交信息设置为`iter{n}_finish`（如第三次迭代为`iter3_finish`）。
2. 除迭代一外，每⼀次迭代需要在上⼀次迭代的最终版上继续更改，在三次迭代结束后我们会检查完整的三次迭代的git提交信息.
3. 除了本地的git追踪外，你还需要在 [git.nju.edu.cn](https://git.nju.edu.cn/) 上创建git仓库进行远程的备份，仓库命名为`software_design_小组号`，**并将仓库设置为private**，在项目三次迭代结束后，我们会收集所有同学的项目链接，并对你的项目实现与文档进行检查。
4. 为了控制提交大小，**请务必通过 `.gitignore`文件来过滤build、target等产物目录**。

### 团队协作

- 请务必遵循团队协作规范，确保代码质量，不要做团队杀手！😡。
- 建议在每位同学阅读完文档后再在小组内进行讨论，确定软件设计并分工进行开发。
- 我们认为迭代三的分工和你们小组的设计相关，所以在本次实验中我们不再提供流程图

### 软件设计

> [!TIP]
>
> **弹性系统设计**
>
> 请从复用性和维护性的角度出发，设计一个弹性的系统，以便应对可能的变更。

1. 小组应该合理把握类的设计，在保证软件可扩展性、可维护性、符合设计原则的同时避免过度设计。
2. 在更深入学习设计模式后，我们鼓励小组对迭代一和迭代二中不合理的设计进行修改。

> [!WARNING]
>
> **实验警告**
>
> - 学生不得复制其他小组的代码和设计文档
> - 学生不得向其他小组提供代码和设计文档
> - 在第三次迭代结束前（4月30日23:59），学生不得公开代码仓库或参考其他小组代码仓库的实现