# 水库 API

[English](README.md) | 简体中文

## 简介
这是一个可以帮你简化在 Fabric 中创建流体的过程的mod。

* Mod ID：reservoir-api

* 注意：本mod尚在beta阶段，可能遭遇bug和崩溃等不稳定现象。使用前请自行斟酌。

* 如果发现任何bug，请及时去issues页面反馈，并尽量详细描述触发方式和影响，以便尽快修复。可接受中文/英语issues。

## 特性
### 注册
* 自定义的流体注册方式
* 用 `ExtendedFluid` 告别大量重写
* 使用 `FluidSettings` 定义流体属性
### 交互行为
* 支持向上流动的流体
* 全新数据驱动流体反应系统
* 支持创建不同材质及可堆叠的流体桶
* 无需标签的实体碰撞交互
* 可自定义的水下迷雾
* 更多在此未提及的功能...

## 适用版本
* 适用MC版本：1.20.1
* Fabric版本：0.15.0或以上
* 前置mod：Fabric API, [Cloth Config API](https://github.com/shedaniel/cloth-config)

## 开发指南
### CurseMaven
1. 按照官方教程添加 CurseMaven 仓库
2. 可选: 下载带源代码的 jar 以便获取 javadoc 等支持
3. 在你的 `build.gradle` 中 `dependencies` 下添加：
   `modImplementation 'curse.maven:rfapi-1649310:VERSIONID'`
4. 将 `VERSIONID` 替换为要使用的版本的 ID
5. 刷新项目
### Modrinth Maven
1. 按照官方教程添加 Modrinth Maven 仓库
2. 可选: 下载带源代码的 jar 以便获取 javadoc 等支持
3. 在你的 `build.gradle` 中 `dependencies` 下添加：
   `modImplementation 'maven.modrinth:rfapi:VERSION'`
4. 将上文中 `VERSION` 替换为要使用的版本
5. 刷新项目
### 手动管理
1. 在项目根目录创建名为 `libs` 的文件夹
2. 将下载好的mod文件放入文件夹，并重命名为 `rfapi.jar`
3. 可选: 下载带源代码的 jar 以便获取 javadoc 等支持
4. 在你的 `build.gradle` 中 `dependencies` 下添加：
   `modImplementation(files('libs/rfapi.jar'))`
5. 在 `modRuntimeOnly` 或 `modImplmention` （推荐在需要配置api时使用）配置中添加 _Cloth Config API_
6. 刷新项目

### 许可协议
本项目受 [LGPL-2.1](LICENSE) 或更新版本 协议保护  
&copy; 2026 redColmula55  
非 _Minecraft_ 官方产品。未经 _Mojang_ 批准或关联。
