package com.itence.mryang.twoboxing.entity.ycsf;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 矩形装箱优化算法 - 遗传算法实现
 * 使用进化算法寻找最优布局方案
 */
public class RectanglePackingGenetic {
    // 大矩形的尺寸
    private static final int CONTAINER_WIDTH = 9000;
    private static final int CONTAINER_HEIGHT = 4000;
    // 遗传算法参数
    private static final int POPULATION_SIZE = 100; // 种群大小
    private static final int MAX_GENERATIONS = 100; // 最大迭代代数
    private static final double MUTATION_RATE = 0.1; // 变异率
    private static final double CROSSOVER_RATE = 0.8; // 交叉率

    /**
     * 判断一个矩形是否与已放置的矩形重叠(考虑外延重叠规则)
     */
    private static boolean overlaps(Rectangle rect, List<Rectangle> placedRectangles) {
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
            double placedInnerX = placed.getInnerX();
            double placedInnerY = placed.getInnerY();
            double placedInnerWidth = placed.getInnerWidth();
            double placedInnerHeight = placed.getInnerHeight();
// 检查内部区域是否重叠
            boolean innerOverlap = !(innerX + innerWidth <= placedInnerX ||
                    placedInnerX + placedInnerWidth <= innerX ||
                    innerY + innerHeight <= placedInnerY ||
                    placedInnerY + placedInnerHeight <= innerY);
            if (innerOverlap) {
                return true; // 内部区域重叠，不允许放置
            }// 实际中还需要检查外延重叠是否符合规则
// 简化处理，仅检查内部区域重叠
        }
        return false; // 没有不符合规则的重叠
    }

    /**
     * 检查矩形是否在容器内(内部区域必须在容器内)
     */
    private static boolean isInsideContainer(Rectangle rect) {
        double innerX = rect.getInnerX();
        double innerY = rect.getInnerY();
        double innerWidth = rect.getInnerWidth();
        double innerHeight = rect.getInnerHeight();
        return innerX >= 0 && innerY >= 0 &&
                innerX + innerWidth <= CONTAINER_WIDTH &&
                innerY + innerHeight <= CONTAINER_HEIGHT;
    }

    /**
     * 计算解决方案的适应度(矩形面积总和与容器面积的比率)
     */
    private static void calculateFitness(Solution solution) {
        double totalArea = 0;
        int validRectangles = 0;
        for (Rectangle rect : solution.rectangles) {
// 检查矩形是否有效放置
            if (rect.x >= 0 && rect.y >= 0 &&
                    isInsideContainer(rect) &&
                    !overlaps(rect, solution.rectangles)) {
                totalArea += rect.getArea();
                validRectangles++;
            }
        }// 适应度是有效放置矩形的面积总和与总矩形数量的乘积
        solution.fitness = totalArea * validRectangles / solution.rectangles.size();
    }

    /**
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
            }
            calculateFitness(solution);
            population.add(solution);
        }
        return population;
    }

    /**
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
            }
        }
        return best;
    }

    /**
     * 交叉操作(单点交叉)
     */
    private static Solution crossover(Solution parent1, Solution parent2) {
        if (parent1.rectangles.size() != parent2.rectangles.size()) {
            throw new IllegalArgumentException("父代解决方案矩形数量不一致");
        }
        Random random = new Random();
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
            }
        }
        return child;
    }

    /**
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
            }
        }
    }

    /**
     * 使用遗传算法优化矩形布局
     */
    public static List<Rectangle> optimizeLayout(List<Rectangle> rectangles) {
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
                System.out.println("第 " + generation + " 代，最佳适应度: " + bestSolution.fitness);
            }
        }// 返回最优解
        Solution finalBestSolution = bestSolution;
        return bestSolution.rectangles.stream()
                .filter(r -> r.x >= 0 && r.y >= 0 && isInsideContainer(r) && !overlaps(r, finalBestSolution.rectangles))
                .collect(Collectors.toList());
    }

    /**
     * 计算布局的总利用率
     */
    public static double calculateUtilization(List<Rectangle> placedRectangles) {
        double totalRectArea = 0;
        for (Rectangle rect : placedRectangles) {
            totalRectArea += rect.getArea();
        }
        double containerArea = CONTAINER_WIDTH * CONTAINER_HEIGHT;
        return totalRectArea / containerArea;
    }

    /**
     * 主函数演示
     */
    public static void main(String[] args) {
// 创建示例矩形列表
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
        rectangles.add(new Rectangle(10, 4620.00, 2020.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(11, 2920.00, 1520.00, 90.00, 90.00, 90.00, 396.00));
        rectangles.add(new Rectangle(12, 4620.00, 2100.00, 90.00, 90.00, 286.00, 286.00));
        rectangles.add(new Rectangle(13, 2620.00, 1470.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(14, 4620.00, 2100.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(15, 2620.00, 1400.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(16, 2620.00, 1470.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(17, 2620.00, 1400.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(18, 4620.00, 2020.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(19, 4620.00, 2100.00, 90.00, 90.00, 286.00, 286.00));
        rectangles.add(new Rectangle(20, 4620.00, 2100.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(31, 3420.00, 1520.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(32, 2920.00, 1600.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(33, 3420.00, 1600.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(34, 3420.00, 1520.00, 90.00, 90.00, 90.00, 286.00));
        rectangles.add(new Rectangle(35, 3420.00, 1600.00, 90.00, 90.00, 286.00, 90.00));
        rectangles.add(new Rectangle(36, 1420.00, 2120.00, 575.00, 286.00, 90.00, 90.00));
        rectangles.add(new Rectangle(37, 1420.00, 2000.00, 286.00, 286.00, 90.00, 90.00));
// 执行遗传算法
        System.out.println("开始遗传算法优化...");
        List<Rectangle> optimizedLayout = optimizeLayout(rectangles);
// 输出结果
        System.out.println("\n优化完成!");
        System.out.println("成功放置的矩形数量: " + optimizedLayout.size());
        System.out.println("空间利用率: " + String.format("%.2f%%", calculateUtilization(optimizedLayout) * 100));
        System.out.println("\n矩形详细信息:");
        for (Rectangle rect : optimizedLayout) {
            System.out.println(rect);
            System.out.printf("\"#%d\": {\"inner\": ((%.0f, %.0f), (%.0f, %.0f)), \"outer\": ((%.0f, %.0f), (%.0f, %.0f))}%n",
                    rect.id,
                    rect.getInnerX(), rect.getInnerY(),
                    rect.getInnerX() + rect.getInnerWidth(), rect.getInnerY() + rect.getInnerHeight(),
                    rect.getOuterX(), rect.getOuterY(),
                    rect.getOuterX() + rect.getTotalWidth(), rect.getOuterY() + rect.getTotalHeight());
            System.out.println();
        }
    }

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
            } else {
                return height + extensionA + extensionB;
            }
        }// 获取矩形实际高度(含外延)

        public double getTotalHeight() {
            if (!rotated) {
                return height + extensionA + extensionB;
            } else {
                return width + extensionC + extensionD;
            }
        }// 获取矩形的内部区域(不含外延)

        public double getInnerWidth() {
            return rotated ? height : width;
        }

        public double getInnerHeight() {
            return rotated ? width : height;
        }// 获取矩形的面积

        public double getArea() {
            return width * height;
        }// 获取矩形的内部区域左上角坐标

        public double getInnerX() {
            return rotated ? x + extensionA : x + extensionC;
        }

        public double getInnerY() {
            return rotated ? y + extensionC : y + extensionA;
        }// 获取矩形的外部区域左上角坐标(包含外延)

        public double getOuterX() {
            return x;
        }

        public double getOuterY() {
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
        }

        @Override
        public String toString() {
            return String.format("矩形#%d - 内部尺寸:%dx%d, 位置:(%f,%f), %s",
                    id, (int) getInnerWidth(), (int) getInnerHeight(), getInnerX(), getInnerY(),
                    rotated ? "已旋转" : "未旋转");
        }
    }

    /**
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
        }
    }

}

