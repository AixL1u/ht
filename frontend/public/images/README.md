# 静态图片资源目录

## 目录结构

```
public/images/
├── projects/      # 非遗项目封面图片
├── products/      # 商品图片  
├── activities/    # 活动封面图片
├── banners/       # 首页轮播图等横幅
└── README.md
```

## 使用说明

### 添加图片
将图片文件放入对应子目录，例如：
- `projects/jingju.jpg` - 京剧
- `projects/kunqu.jpg` - 昆曲
- `products/ciuxi-bindai.jpg` - 刺绣抱枕
- `activities/taijiquan.jpg` - 太极拳活动

### 在数据库中引用
```sql
UPDATE heritage_projects SET cover_image = '/images/projects/jingju.jpg' WHERE name = '京剧';
UPDATE products SET image = '/images/products/ciuxi-bindai.jpg' WHERE name = '刺绣抱枕';
UPDATE activities SET cover_image = '/images/activities/taijiquan.jpg' WHERE title = '太极拳养生班';
```

### 访问路径
图片可通过 `/images/xxx` 直接访问，例如：
- `http://localhost:5173/images/projects/jingju.jpg`

## 图片规格建议

| 类型 | 推荐尺寸 | 格式 |
|------|----------|------|
| 项目封面 | 800×600 | jpg/webp |
| 商品图片 | 600×600 | jpg/webp |
| 活动封面 | 800×450 | jpg/webp |
| 轮播横幅 | 1920×600 | jpg/webp |

## 临时占位图
如暂无真实图片，数据库中可使用在线图片服务：
- `https://picsum.photos/800/600`
- `https://via.placeholder.com/800x600`
