package com.itence.mryang.twoboxing.entity.txsf2;

import java.util.ArrayList;
import java.util.List;

/**
 * 矩形装箱优化算法 - 贪心算法实现
 * 解决在固定大小矩形内放置多个小矩形，且考虑外延和重叠的问题
 */
public class RectanglePackingGreedy {
    // 大矩形的尺寸
    private static final int CONTAINER_WIDTH = 9000;
    private static final int CONTAINER_HEIGHT = 4000;

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
            }// 检查外延重叠符合规则
// 这里需要实现复杂的外延重叠规则判断
// 此处简化，仅当一个矩形的内部区域与另一个矩形的外延区域重叠时不允许放置
        }
        return false; // 没有不符合规则的重叠
    }

    /**
     * 查找矩形的最佳放置位置
     */
    private static Position findBestPosition(Rectangle rect, List<Rectangle> placedRectangles, int containerWidth, int containerHeight) {
    List<Position> candidatePositions = new ArrayList<>();
    candidatePositions.add(new Position(0, 0, false));
    candidatePositions.add(new Position(0, 0, true));

    for (Rectangle placed : placedRectangles) {
        candidatePositions.add(new Position(
                placed.getInnerX() + placed.getInnerWidth(),
                placed.getInnerY(),
                false));
        candidatePositions.add(new Position(
                placed.getInnerX() + placed.getInnerWidth(),
                placed.getInnerY(),
                true));
        candidatePositions.add(new Position(
                placed.getInnerX(),
                placed.getInnerY() + placed.getInnerHeight(),
                false));
        candidatePositions.add(new Position(
                placed.getInnerX(),
                placed.getInnerY() + placed.getInnerHeight(),
                true));
    }

    Position bestPosition = null;
    double bestScore = Integer.MAX_VALUE;

    for (Position pos : candidatePositions) {
        Rectangle testRect = rect.copy();
        testRect.x = pos.x;
        testRect.y = pos.y;
        if (pos.rotated) {
            testRect.rotate();
        }

        double innerX = testRect.getInnerX();
        double innerY = testRect.getInnerY();
        double innerWidth = testRect.getInnerWidth();
        double innerHeight = testRect.getInnerHeight();

        if (innerX < 0 || innerY < 0 ||
            innerX + innerWidth > containerWidth ||
            innerY + innerHeight > containerHeight) {
            continue; // 超出容器范围
        }

        if (overlaps(testRect, placedRectangles)) {
            continue; // 重叠
        }

        double score = innerX + innerY;
        if (score < bestScore) {
            bestScore = score;
            bestPosition = pos;
        }
    }

    return bestPosition;
}


    /**
     * 使用贪心算法放置矩形
     */
   public static List<Container> packRectangles(List<Rectangle> rectangles, int containerWidth, int containerHeight) {
    List<Container> containers = new ArrayList<>();
    containers.add(new Container(1, containerWidth, containerHeight)); // 初始化第一个容器

    for (Rectangle rect : rectangles) {
        boolean placed = false;

        // 尝试在现有的容器中放置矩形
        for (Container container : containers) {
            Position bestPos = findBestPosition(rect, container.getPlacedRectangles(), container.getWidth(), container.getHeight());
            if (bestPos != null) {
                Rectangle placedRect = rect.copy();
                placedRect.x = bestPos.x;
                placedRect.y = bestPos.y;
                if (bestPos.rotated) {
                    placedRect.rotate();
                }
                container.place(placedRect);
                placed = true;
                break;
            }
        }

        // 如果所有现有容器都无法放置，则创建新容器
        if (!placed) {
            Container newContainer = new Container(containers.size() + 1, containerWidth, containerHeight);
            Position bestPos = findBestPosition(rect, newContainer.getPlacedRectangles(), containerWidth, containerHeight);
            if (bestPos != null) {
                Rectangle placedRect = rect.copy();
                placedRect.x = bestPos.x;
                placedRect.y = bestPos.y;
                if (bestPos.rotated) {
                    placedRect.rotate();
                }
                newContainer.place(placedRect);
                containers.add(newContainer);
            }
        }
    }

    return containers;
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

        int containerWidth = 9000;
        int containerHeight = 4000;
        // 执行贪心算法
        List<Container> containers = packRectangles(rectangles, containerWidth, containerHeight);

        System.out.println("使用的容器数量: " + containers.size());
        for (Container container : containers) {
            System.out.println("\n容器 #" + container.getId() + ":");
            for (Rectangle rect : container.getPlacedRectangles()) {
                System.out.println(String.format("\"#%d\": {\"inner\": ((%.0f, %.0f), (%.0f, %.0f)), \"outer\": ((%.0f, %.0f), (%.0f, %.0f))}",
                        rect.id,
                        rect.getInnerX(), rect.getInnerY(),
                        rect.getInnerX() + rect.getInnerWidth(), rect.getInnerY() + rect.getInnerHeight(),
                        rect.getOuterX(), rect.getOuterY(),
                        rect.getOuterX() + rect.getTotalWidth(), rect.getOuterY() + rect.getTotalHeight()));
            }
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
            // 使用 %f 格式化 double 类型，或者将 double 转换为 int
            return String.format("矩形#%d - 内部尺寸:%dx%d, 位置:(%f,%f), %s",
                    id, (int)getInnerWidth(), (int)getInnerHeight(), getInnerX(), getInnerY(),
                    rotated ? "已旋转" : "未旋转");
        }
    }

    /**
     * 表示矩形布局的位置
     */
    static class Position {
        double x, y;
        boolean rotated;

        public Position(double x, double y, boolean rotated) {
            this.x = x;
            this.y = y;
            this.rotated = rotated;
        }
    }


    static class Container {
        private final int id;
        private final int width;
        private final int height;
        private final List<Rectangle> placedRectangles = new ArrayList<>();

        public Container(int id, int width, int height) {
            this.id = id;
            this.width = width;
            this.height = height;
        }

        public int getId() {
            return id;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public List<Rectangle> getPlacedRectangles() {
            return placedRectangles;
        }

        public boolean canPlace(Rectangle rect) {
            double innerX = rect.getInnerX();
            double innerY = rect.getInnerY();
            double innerWidth = rect.getInnerWidth();
            double innerHeight = rect.getInnerHeight();
            return innerX >= 0 && innerY >= 0 &&
                    innerX + innerWidth <= width &&
                    innerY + innerHeight <= height &&
                    !overlaps(rect, placedRectangles);
        }

        public void place(Rectangle rect) {
            placedRectangles.add(rect);
        }
    }

}
