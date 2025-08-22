package com.itence.mryang.twoboxing.entity.txyc;

import com.itence.mryang.twoboxing.entity.ycsf.RectanglePackingGenetic;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 矩形装箱优化算法 - 完整应用
 * 包含贪心算法和遗传算法两种实现
 */
public class RectanglePackingApp {
    // 大矩形的尺寸
    private static final int CONTAINER_WIDTH = 9000;
    private static final int CONTAINER_HEIGHT = 4000;
    // 遗传算法参数
    private static final int POPULATION_SIZE = 100;
    private static final int MAX_GENERATIONS = 100;
    private static final double MUTATION_RATE = 0.1;
    private static final double CROSSOVER_RATE = 0.8;
    /**
     * 表示一个矩形及其四边外延
     */
    static class Rectangle {
        double width; // 矩形宽度
        double height; // 矩形高度
        double extensionA; // 上边外延
        double extensionB; // 下边外延
        double extensionC; // 左边外延
        double extensionD; // 右边外延
        double x; // 放置位置x坐标
        double y; // 放置位置y坐标
        boolean rotated; // 是否旋转90度
        int id; // 矩形标识
        public Rectangle(int id, double width, double height, double extensionA, double extensionB,
                         double extensionC, double extensionD) {
            this.id = id;
            this.width = width;
            this.height = height;
            this.extensionA = extensionA;
            this.extensionB = extensionB;
            this.extensionC = extensionC;
            this.extensionD = extensionD;
            this.x = -1;
            this.y = -1;
            this.rotated = false;
        }// 获取矩形实际宽度(含外延)
        public double getTotalWidth() {
            if (!rotated) {
                return width + extensionC + extensionD;
            }else {
                return height + extensionA + extensionB;
            }}// 获取矩形实际高度(含外延)
        public double getTotalHeight() {
            if (!rotated) {
                return height + extensionA + extensionB;
            }else {
                return width + extensionC + extensionD;
            }}// 获取矩形的内部区域(不含外延)
        public double getInnerWidth() {
            return rotated ? height : width;
        }public double getInnerHeight() {
            return rotated ? width : height;
        }// 获取矩形的面积
        public double getArea() {
            return width * height;
        }// 获取矩形的内部区域左上角坐标
        public double getInnerX() {
            return rotated ? x + extensionA : x + extensionC;
        }public double getInnerY() {
            return rotated ? y + extensionC : y + extensionA;
        }// 获取矩形的外部区域左上角坐标(包含外延)
        public double getOuterX() {
            return x;
        }public double getOuterY() {
            return y;
        }// 旋转矩形90度
        public void rotate() {
            rotated = !rotated;
        }// 深拷贝矩形
        public Rectangle copy() {
            Rectangle r = new Rectangle(id, width, height, extensionA, extensionB, extensionC, extensionD);
            r.x = this.x;
            r.y = this.y;
            r.rotated = this.rotated;
            return r;
        }@Override
        public String toString() {
            return String.format("矩形#%d - 内部尺寸:%dx%d, 位置:(%d,%d), %s",
                    id, getInnerWidth(), getInnerHeight(), getInnerX(), getInnerY(),
                    rotated ? "已旋转" : "未旋转");
        }}/**
     * 代表一个布局方案(染色体)
     */
    static class Solution {
        List<Rectangle> rectangles; // 矩形列表
        double fitness; // 适应度(利用率)
        public Solution(List<Rectangle> rectangles) {
            this.rectangles = rectangles.stream()
                    .map(Rectangle::copy)
                    .collect(Collectors.toList());
            this.fitness = 0;
        }// 深拷贝解决方案
        public Solution copy() {
            return new Solution(this.rectangles);
        }}/**
     * 判断一个矩形是否与已放置的矩形重叠(考虑外延重叠规则)
     */
    private static boolean hasInvalidOverlap(Rectangle rect, List<Rectangle> placedRectangles) {
// 获取矩形的内部区域坐标
        double innerX = rect.getInnerX();
        double innerY = rect.getInnerY();
        double innerWidth = rect.getInnerWidth();
        double innerHeight = rect.getInnerHeight();
// 检查每个已放置的矩形
        for (Rectangle placed : placedRectangles) {
// 跳过自身
            if (placed.id == rect.id) {
                continue;
            }
// 获取已放置矩形的内部区域坐标
            double placedInnerX = placed.getInnerX();
            double placedInnerY = placed.getInnerY();
            double placedInnerWidth = placed.getInnerWidth();
            double placedInnerHeight = placed.getInnerHeight();
// 检查内部区域是否重叠 - 不允许内部区域重叠
            boolean innerOverlap = !(innerX + innerWidth <= placedInnerX ||
                    placedInnerX + placedInnerWidth <= innerX ||
                    innerY + innerHeight <= placedInnerY ||
                    placedInnerY + placedInnerHeight <= innerY);
            if (innerOverlap) {
                return true; // 内部区域重叠，这是不允许的
            }// 检查外延重叠是否符合规则
// 首先确定两个矩形的相对位置关系
// Case 1: rect在placed的右侧
            if (innerX >= placedInnerX + placedInnerWidth) {
                double rectLeftExtension = rect.rotated ? rect.extensionA : rect.extensionC;
                double placedRightExtension = placed.rotated ? placed.extensionB : placed.extensionD;
// 检查外延重叠是否符合规则
                if (innerX - rectLeftExtension < placedInnerX + placedInnerWidth) {
// 有外延重叠
                    double overlap = placedInnerX + placedInnerWidth - (innerX - rectLeftExtension);
                    if (overlap > Math.min(rectLeftExtension, placedRightExtension)) {
                        return true; // 重叠超过了允许的外延
                    }}}// Case 2: rect在placed的左侧
            if (innerX + innerWidth <= placedInnerX) {
                double rectRightExtension = rect.rotated ? rect.extensionB : rect.extensionD;
                double placedLeftExtension = placed.rotated ? placed.extensionA : placed.extensionC;
// 检查外延重叠是否符合规则
                if (innerX + innerWidth + rectRightExtension > placedInnerX - placedLeftExtension) {
// 有外延重叠
                    double overlap = (innerX + innerWidth + rectRightExtension) - (placedInnerX - placedLeftExtension);
                    if (overlap > Math.min(rectRightExtension, placedLeftExtension)) {
                        return true; // 重叠超过了允许的外延
                    }}}// Case 3: rect在placed的下方
            if (innerY >= placedInnerY + placedInnerHeight) {
                double rectTopExtension = rect.rotated ? rect.extensionC : rect.extensionA;
                double placedBottomExtension = placed.rotated ? placed.extensionD : placed.extensionB;
// 检查外延重叠是否符合规则
                if (innerY - rectTopExtension < placedInnerY + placedInnerHeight) {
// 有外延重叠
                    double overlap = placedInnerY + placedInnerHeight - (innerY - rectTopExtension);
                    if (overlap > Math.min(rectTopExtension, placedBottomExtension)) {
                        return true; // 重叠超过了允许的外延
                    }}}// Case 4: rect在placed的上方
            if (innerY + innerHeight <= placedInnerY) {
                double rectBottomExtension = rect.rotated ? rect.extensionD : rect.extensionB;
                double placedTopExtension = placed.rotated ? placed.extensionC : placed.extensionA;
// 检查外延重叠是否符合规则
                if (innerY + innerHeight + rectBottomExtension > placedInnerY - placedTopExtension) {
// 有外延重叠
                    double overlap = (innerY + innerHeight + rectBottomExtension) - (placedInnerY - placedTopExtension);
                    if (overlap > Math.min(rectBottomExtension, placedTopExtension)) {
                        return true; // 重叠超过了允许的外延
                    }}}}return false; // 没有不合规则的重叠
    }/**
     * 检查矩形内部区域是否在容器内(外延可以超出)
     */
    private static boolean isInsideContainer(Rectangle rect) {
        double innerX = rect.getInnerX();
        double innerY = rect.getInnerY();
        double innerWidth = rect.getInnerWidth();
        double innerHeight = rect.getInnerHeight();
        return innerX >= 0 && innerY >= 0 &&
                innerX + innerWidth <= CONTAINER_WIDTH &&
                innerY + innerHeight <= CONTAINER_HEIGHT;
    }/**
     * 表示矩形布局的位置
     */
    static class Position {
        double x, y;
        boolean rotated;
        public Position(double x, double y, boolean rotated) {
            this.x = x;
            this.y = y;
            this.rotated = rotated;
        }}/**
     * 查找矩形的最佳放置位置(贪心算法)
     */
    private static Position findBestPosition(Rectangle rect, List<Rectangle> placedRectangles) {
// 试探不同放置位置
        List<Position> candidatePositions = new ArrayList<>();
// 先尝试左上角(0,0)
        candidatePositions.add(new Position(0, 0, false));
        candidatePositions.add(new Position(0, 0, true));
// 然后尝试已放置矩形的右侧和下侧位置
        for (Rectangle placed : placedRectangles) {
// 放在已放置矩形的右侧
            candidatePositions.add(new Position(
                    placed.getInnerX() + placed.getInnerWidth(),
                    placed.getInnerY(),
                    false));
            candidatePositions.add(new Position(
                    placed.getInnerX() + placed.getInnerWidth(),
                    placed.getInnerY(),
                    true));
// 放在已放置矩形的下侧
            candidatePositions.add(new Position(
                    placed.getInnerX(),
                    placed.getInnerY() + placed.getInnerHeight(),
                    false));
            candidatePositions.add(new Position(
                    placed.getInnerX(),
                    placed.getInnerY() + placed.getInnerHeight(),
                    true));
        }// 选择最优位置(尽量靠左上)
        Position bestPosition = null;
        double bestScore = Integer.MAX_VALUE;
        for (Position pos : candidatePositions) {
            Rectangle testRect = rect.copy();
            testRect.x = pos.x;
            testRect.y = pos.y;
            if (pos.rotated) {
                testRect.rotate();
            }// 检查内部区域是否在容器内
            if (!isInsideContainer(testRect)) {
                continue;
            }// 检查是否与已放置的矩形有不符合规则的重叠
            if (hasInvalidOverlap(testRect, placedRectangles)) {
                continue;
            }// 计算位置得分(优先左上角位置)
            double score = testRect.getInnerX() + testRect.getInnerY();
            if (score < bestScore) {
                bestScore = score;
                bestPosition = pos;
            }}return bestPosition;
    }/**
     * 使用贪心算法放置矩形
     */
    public static List<Rectangle> packRectanglesGreedy(List<Rectangle> rectangles) {
// 按面积从大到小排序
        List<Rectangle> sortedRectangles = rectangles.stream()
                .map(Rectangle::copy)
                .sorted((r1, r2) -> Double.compare(r2.getArea(), r1.getArea()))
                .collect(Collectors.toList());
        List<Rectangle> placedRectangles = new ArrayList<>();
        List<Rectangle> unplacedRectangles = new ArrayList<>();
// 尝试放置每个矩形
        for (Rectangle rect : sortedRectangles) {
            Position bestPos = findBestPosition(rect, placedRectangles);
            if (bestPos != null) {
                Rectangle placedRect = rect.copy();
                placedRect.x = bestPos.x;
                placedRect.y = bestPos.y;
                if (bestPos.rotated) {
                    placedRect.rotate();
                }placedRectangles.add(placedRect);
            }else {
                unplacedRectangles.add(rect);
            }}// 输出未放置的矩形数量
        if (!unplacedRectangles.isEmpty()) {
            System.out.println("贪心算法: 有 " + unplacedRectangles.size() + " 个矩形无法放置");
        }return placedRectangles;
    }/**
     * 计算解决方案的适应度(矩形面积总和与容器面积的比率)
     */
    private static void calculateFitness(Solution solution) {
        double totalArea = 0;
        int validRectangles = 0;
        for (Rectangle rect : solution.rectangles) {
// 检查矩形是否有效放置
            if (rect.x >= 0 && rect.y >= 0 &&
                    isInsideContainer(rect) &&
                    !hasInvalidOverlap(rect, solution.rectangles)) {
                totalArea += rect.getArea();
                validRectangles++;
            }}// 适应度是有效放置矩形的面积总和与总矩形数量的乘积
        solution.fitness = totalArea * validRectangles / solution.rectangles.size();
    }/**
     * 创建初始种群
     */
    private static List<Solution> createInitialPopulation(List<Rectangle> rectangles) {
        List<Solution> population = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Solution solution = new Solution(rectangles);
// 随机放置每个矩形
            for (Rectangle rect : solution.rectangles) {
                rect.x = random.nextInt(CONTAINER_WIDTH);
                rect.y = random.nextInt(CONTAINER_HEIGHT);
                rect.rotated = random.nextBoolean();
            }calculateFitness(solution);
            population.add(solution);
        }return population;
    }/**
     * 选择父代(锦标赛选择)
     */
    private static Solution tournamentSelection(List<Solution> population) {
        Random random = new Random();
        int tournamentSize = 5;
        Solution best = null;
        for (int i = 0; i < tournamentSize; i++) {
            int idx = random.nextInt(population.size());
            Solution candidate = population.get(idx);
            if (best == null || candidate.fitness > best.fitness) {
                best = candidate;
            }}return best;
    }/**
     * 交叉操作(单点交叉)
     */
    private static Solution crossover(Solution parent1, Solution parent2) {
        if (parent1.rectangles.size() != parent2.rectangles.size()) {
            throw new IllegalArgumentException("父代解决方案矩形数量不一致");
        }Random random = new Random();
        Solution child = new Solution(parent1.rectangles);
        if (random.nextDouble() < CROSSOVER_RATE) {
            int crossoverPoint = random.nextInt(parent1.rectangles.size());
            for (int i = 0; i < parent1.rectangles.size(); i++) {
                Rectangle sourceRect = i < crossoverPoint ?
                        parent1.rectangles.get(i) :
                        parent2.rectangles.get(i);
                child.rectangles.get(i).x = sourceRect.x;
                child.rectangles.get(i).y = sourceRect.y;
                child.rectangles.get(i).rotated = sourceRect.rotated;
            }}return child;
    }/**
     * 变异操作
     */
    private static void mutate(Solution solution) {
        Random random = new Random();
        for (Rectangle rect : solution.rectangles) {
// 位置变异
            if (random.nextDouble() < MUTATION_RATE) {
                rect.x = random.nextInt(CONTAINER_WIDTH);
                rect.y = random.nextInt(CONTAINER_HEIGHT);
            }// 旋转变异
            if (random.nextDouble() < MUTATION_RATE) {
                rect.rotated = !rect.rotated;
            }}}/**
     * 使用遗传算法优化矩形布局
     */
    public static List<Rectangle> packRectanglesGenetic(List<Rectangle> rectangles) {
// 创建初始种群
        List<Solution> population = createInitialPopulation(rectangles);
        Solution bestSolution = null;
// 迭代进化
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
// 找出当前最优解
            Solution currentBest = Collections.max(population, Comparator.comparing(s -> s.fitness));
            if (bestSolution == null || currentBest.fitness > bestSolution.fitness) {
                bestSolution = currentBest.copy();
            }// 创建新一代
            List<Solution> newPopulation = new ArrayList<>();
// 保留精英
            newPopulation.add(currentBest.copy());
// 生成其余个体
            while (newPopulation.size() < POPULATION_SIZE) {
// 选择父代
                Solution parent1 = tournamentSelection(population);
                Solution parent2 = tournamentSelection(population);
// 交叉
                Solution offspring = crossover(parent1, parent2);
// 变异
                mutate(offspring);
// 计算适应度
                calculateFitness(offspring);
// 添加到新种群
                newPopulation.add(offspring);
            }// 更新种群
            population = newPopulation;
// 输出当前代进度
            if (generation % 10 == 0) {
                System.out.println("遗传算法: 第 " + generation + " 代，最佳适应度: " + bestSolution.fitness);
            }}// 返回最优解中的有效矩形
        Solution finalBestSolution = bestSolution;
        return bestSolution.rectangles.stream()
                .filter(r -> r.x >= 0 && r.y >= 0 &&
                        isInsideContainer(r) &&
                        !hasInvalidOverlap(r, finalBestSolution.rectangles))
                .collect(Collectors.toList());
    }/**
     * 计算布局的总利用率
     */
    public static double calculateUtilization(List<Rectangle> placedRectangles) {
        double totalRectArea = 0;
        for (Rectangle rect : placedRectangles) {
            totalRectArea += rect.getArea();
        }double containerArea = CONTAINER_WIDTH * CONTAINER_HEIGHT;
        return totalRectArea / containerArea;
    }/**
     * 打印布局的详细信息
     */
    public static void printLayoutDetails(List<Rectangle> placedRectangles, String algorithmName) {
        System.out.println("\n===== " + algorithmName + " 布局结果 =====");
        System.out.println("成功放置的矩形数量: " + placedRectangles.size());
        System.out.println("空间利用率: " + String.format("%.2f%%", calculateUtilization(placedRectangles) * 100));
        System.out.println("\n矩形详细信息:");
        for (Rectangle rect : placedRectangles) {
            System.out.println(String.format("\"#%d\": {\"inner\": ((%.0f, %.0f), (%.0f, %.0f)), \"outer\": ((%.0f, %.0f), (%.0f, %.0f))}",
                    rect.id,
                    rect.getInnerX(), rect.getInnerY(),
                    rect.getInnerX() + rect.getInnerWidth(), rect.getInnerY() + rect.getInnerHeight(),
                    rect.getOuterX(), rect.getOuterY(),
                    rect.getOuterX() + rect.getTotalWidth(), rect.getOuterY() + rect.getTotalHeight()));
        }}/**
     * 生成随机矩形
     */
    public static List<Rectangle> generateRandomRectangles(int count) {
        List<Rectangle> rectangles = new ArrayList<>();
        Random random = new Random();
        for (int i = 1; i <= count; i++) {
// 随机生成矩形尺寸(限制在合理范围内)
            int width = random.nextInt(1500) + 500; // 500-2000
            int height = random.nextInt(1000) + 300; // 300-1300
// 随机生成外延尺寸(限制在合理范围内)
            int extensionA = random.nextInt(30) + 5; // 5-35
            int extensionB = random.nextInt(30) + 5;
            int extensionC = random.nextInt(30) + 5;
            int extensionD = random.nextInt(30) + 5;
            rectangles.add(new Rectangle(i, width, height, extensionA, extensionB, extensionC, extensionD));
        }return rectangles;
    }/**
     * 主函数
     */
    public static void main(String[] args) {
// 创建示例矩形列表(可以根据需要修改)
        List<Rectangle> rectangles = new ArrayList<>();
// 添加一些测试矩形
        rectangles.add(new Rectangle(1, 2820.00, 1600.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(2, 2920.00, 1600.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(3, 2820.00, 920.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(4, 2920.00, 1520.00, 90.00, 90.00, 90.00, 396.00));
        rectangles.add(new Rectangle(5, 2520.00, 1100.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(6, 1220.00, 1270.00, 90.00, 90.00, 90.00, 90.00));
        rectangles.add(new Rectangle(7, 1220.00, 1270.00, 90.00, 90.00, 90.00, 90.00));
        rectangles.add(new Rectangle(8, 1070.00, 1920.00, 90.00, 90.00, 90.00, 90.00));
        rectangles.add(new Rectangle(9, 1070.00, 1920.00, 90.00, 90.00, 90.00, 90.00));
        rectangles.add(new Rectangle(10,4620.00, 2020.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(11,2920.00, 1520.00, 90.00, 90.00, 90.00, 396.00));
        rectangles.add(new Rectangle(12,4620.00, 2100.00, 90.00, 90.00, 286.00, 286.00));
        rectangles.add(new Rectangle(13,2620.00, 1470.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(14,4620.00, 2100.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(15,2620.00, 1400.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(16,2620.00, 1470.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(17,2620.00, 1400.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(18,4620.00, 2020.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(19,4620.00, 2100.00, 90.00, 90.00, 286.00, 286.00));
        rectangles.add(new Rectangle(20,4620.00, 2100.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(31,3420.00, 1520.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(32,2920.00, 1600.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(33,3420.00, 1600.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(34,3420.00, 1520.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(35,3420.00, 1600.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(36,1420.00, 2120.00, 575.00, 286.00, 90.00, 90.00));
        rectangles.add(new Rectangle(37,1420.00, 2000.00, 286.00, 286.00, 90.00, 90.00));
// 或者生成随机矩形
// List<Rectangle> rectangles = generateRandomRectangles(20);
// 执行贪心算法
        System.out.println("开始执行贪心算法...");
        List<Rectangle> greedyResult = packRectanglesGreedy(rectangles);
        printLayoutDetails(greedyResult, "贪心算法");
// 执行遗传算法
        System.out.println("\n开始执行遗传算法...");
        List<Rectangle> geneticResult = packRectanglesGenetic(rectangles);
        printLayoutDetails(geneticResult, "遗传算法");
// 比较两种算法的结果
        System.out.println("\n===== 算法比较 =====");
        System.out.println("贪心算法放置矩形数量: " + greedyResult.size());
        System.out.println("贪心算法空间利用率: " + String.format("%.2f%%", calculateUtilization(greedyResult) * 100));
        System.out.println("遗传算法放置矩形数量: " + geneticResult.size());
        System.out.println("遗传算法空间利用率: " + String.format("%.2f%%", calculateUtilization(geneticResult) * 100));
    }}
