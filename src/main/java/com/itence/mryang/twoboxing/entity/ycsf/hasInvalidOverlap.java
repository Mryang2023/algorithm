//package com.itence.mryang.twoboxing.entity.ycsf;
///**
// * 改进版矩形重叠检测函数，精确处理外延重叠规则
// *
// * @param rect 待检测的矩形
// * @param placedRectangles 已放置的矩形列表
// * @return 是否有不符合规则的重叠
// */
//private static boolean hasInvalidOverlap(Rectangle rect, List<Rectangle> placedRectangles) {
//// 获取矩形的内部区域坐标
//    int innerX = rect.getInnerX();
//    int innerY = rect.getInnerY();
//    int innerWidth = rect.getInnerWidth();
//    int innerHeight = rect.getInnerHeight();
//// 检查每个已放置的矩形
//    for (Rectangle placed : placedRectangles) {
//// 跳过自身
//        if (placed.id == rect.id) continue;
//// 获取已放置矩形的内部区域坐标
//        int placedInnerX = placed.getInnerX();
//        int placedInnerY = placed.getInnerY();
//        int placedInnerWidth = placed.getInnerWidth();
//        int placedInnerHeight = placed.getInnerHeight();
//// 检查内部区域是否重叠 - 不允许内部区域重叠
//        boolean innerOverlap = !(innerX + innerWidth <= placedInnerX ||
//                placedInnerX + placedInnerWidth <= innerX ||
//                innerY + innerHeight <= placedInnerY ||
//                placedInnerY + placedInnerHeight <= innerY);
//        if (innerOverlap) {
//            return true; // 内部区域重叠，这是不允许的
//        }// 检查外延重叠是否符合规则
//// 首先确定两个矩形的相对位置关系
//// Case 1: rect在placed的右侧
//        if (innerX >= placedInnerX + placedInnerWidth) {
//            int rectLeftExtension = rect.rotated ? rect.extensionA : rect.extensionC;
//            int placedRightExtension = placed.rotated ? placed.extensionB : placed.extensionD;
//// 检查外延重叠是否符合规则
//            if (innerX - rectLeftExtension < placedInnerX + placedInnerWidth) {
//// 有外延重叠
//                int overlap = placedInnerX + placedInnerWidth - (innerX - rectLeftExtension);
//                if (overlap > Math.min(rectLeftExtension, placedRightExtension)) {
//                    return true; // 重叠超过了允许的外延
//                }}}// Case 2: rect在placed的左侧
//        if (innerX + innerWidth <= placedInnerX) {
//            int rectRightExtension = rect.rotated ? rect.extensionB : rect.extensionD;
//            int placedLeftExtension = placed.rotated ? placed.extensionA : placed.extensionC;
//// 检查外延重叠是否符合规则
//            if (innerX + innerWidth + rectRightExtension > placedInnerX - placedLeftExtension) {
//// 有外延重叠
//                int overlap = (innerX + innerWidth + rectRightExtension) - (placedInnerX - placedLeftExtension);
//                if (overlap > Math.min(rectRightExtension, placedLeftExtension)) {
//                    return true; // 重叠超过了允许的外延
//                }}}// Case 3: rect在placed的下方
//        if (innerY >= placedInnerY + placedInnerHeight) {
//            int rectTopExtension = rect.rotated ? rect.extensionC : rect.extensionA;
//            int placedBottomExtension = placed.rotated ? placed.extensionD : placed.extensionB;
//// 检查外延重叠是否符合规则
//            if (innerY - rectTopExtension < placedInnerY + placedInnerHeight) {
//// 有外延重叠
//                int overlap = placedInnerY + placedInnerHeight - (innerY - rectTopExtension);
//                if (overlap > Math.min(rectTopExtension, placedBottomExtension)) {
//                    return true; // 重叠超过了允许的外延
//                }}}// Case 4: rect在placed的上方
//        if (innerY + innerHeight <= placedInnerY) {
//            int rectBottomExtension = rect.rotated ? rect.extensionD : rect.extensionB;
//            int placedTopExtension = placed.rotated ? placed.extensionC : placed.extensionA;
//// 检查外延重叠是否符合规则
//            if (innerY + innerHeight + rectBottomExtension > placedInnerY - placedTopExtension) {
//// 有外延重叠
//                int overlap = (innerY + innerHeight + rectBottomExtension) - (placedInnerY - placedTopExtension);
//                if (overlap > Math.min(rectBottomExtension, placedTopExtension)) {
//                    return true; // 重叠超过了允许的外延
//                }}}}return false; // 没有不合规则的重叠
//}
