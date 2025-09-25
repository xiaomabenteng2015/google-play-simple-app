package com.en.teach.data

import com.en.teach.model.Word

class WordRepository {
    
    private val words = mutableListOf<Word>()
    
    init {
        initializeWords()
    }
    
    private fun initializeWords() {
        words.addAll(listOf(
            // 基础单词 (1-20)
            Word(1, "apple", "苹果", "/ˈæpl/", "I eat an apple every day.", "我每天吃一个苹果。"),
            Word(2, "book", "书", "/bʊk/", "This is a good book.", "这是一本好书。"),
            Word(3, "cat", "猫", "/kæt/", "The cat is sleeping.", "猫在睡觉。"),
            Word(4, "dog", "狗", "/dɔːɡ/", "My dog is very friendly.", "我的狗很友好。"),
            Word(5, "elephant", "大象", "/ˈelɪfənt/", "The elephant is huge.", "大象很巨大。"),
            Word(6, "flower", "花", "/ˈflaʊər/", "The flower smells good.", "花闻起来很香。"),
            Word(7, "guitar", "吉他", "/ɡɪˈtɑːr/", "He plays the guitar well.", "他吉他弹得很好。"),
            Word(8, "house", "房子", "/haʊs/", "This is my house.", "这是我的房子。"),
            Word(9, "ice", "冰", "/aɪs/", "The ice is cold.", "冰很冷。"),
            Word(10, "juice", "果汁", "/dʒuːs/", "Orange juice is delicious.", "橙汁很美味。"),
            Word(11, "key", "钥匙", "/kiː/", "I lost my key.", "我丢了钥匙。"),
            Word(12, "lamp", "灯", "/læmp/", "Turn on the lamp.", "打开灯。"),
            Word(13, "moon", "月亮", "/muːn/", "The moon is bright tonight.", "今晚月亮很亮。"),
            Word(14, "notebook", "笔记本", "/ˈnoʊtbʊk/", "I write in my notebook.", "我在笔记本上写字。"),
            Word(15, "ocean", "海洋", "/ˈoʊʃən/", "The ocean is vast.", "海洋很广阔。"),
            Word(16, "pen", "钢笔", "/pen/", "I write with a pen.", "我用钢笔写字。"),
            Word(17, "queen", "女王", "/kwiːn/", "The queen is kind.", "女王很善良。"),
            Word(18, "rain", "雨", "/reɪn/", "It's going to rain today.", "今天要下雨。"),
            Word(19, "sun", "太阳", "/sʌn/", "The sun is shining.", "太阳在照耀。"),
            Word(20, "tree", "树", "/triː/", "The tree is very tall.", "这棵树很高。"),

            // 日常用品 (21-40)
            Word(21, "chair", "椅子", "/tʃer/", "Please sit on the chair.", "请坐在椅子上。"),
            Word(22, "table", "桌子", "/ˈteɪbl/", "Put the book on the table.", "把书放在桌子上。"),
            Word(23, "window", "窗户", "/ˈwɪndoʊ/", "Open the window please.", "请打开窗户。"),
            Word(24, "door", "门", "/dɔːr/", "Close the door behind you.", "请随手关门。"),
            Word(25, "phone", "电话", "/foʊn/", "My phone is ringing.", "我的电话在响。"),
            Word(26, "computer", "电脑", "/kəmˈpjuːtər/", "I work on my computer.", "我在电脑上工作。"),
            Word(27, "car", "汽车", "/kɑːr/", "My car is red.", "我的车是红色的。"),
            Word(28, "bicycle", "自行车", "/ˈbaɪsɪkl/", "I ride my bicycle to school.", "我骑自行车上学。"),
            Word(29, "clock", "时钟", "/klɑːk/", "The clock shows 3 o'clock.", "时钟显示3点。"),
            Word(30, "mirror", "镜子", "/ˈmɪrər/", "Look at yourself in the mirror.", "照照镜子。"),
            Word(31, "bag", "包", "/bæɡ/", "I carry my bag to work.", "我带着包去上班。"),
            Word(32, "shoes", "鞋子", "/ʃuːz/", "These shoes are comfortable.", "这双鞋很舒服。"),
            Word(33, "hat", "帽子", "/hæt/", "I wear a hat in summer.", "夏天我戴帽子。"),
            Word(34, "watch", "手表", "/wɑːtʃ/", "My watch is expensive.", "我的手表很贵。"),
            Word(35, "camera", "相机", "/ˈkæmərə/", "I take photos with my camera.", "我用相机拍照。"),
            Word(36, "umbrella", "雨伞", "/ʌmˈbrelə/", "Take an umbrella with you.", "带把雨伞。"),
            Word(37, "bottle", "瓶子", "/ˈbɑːtl/", "The bottle is full of water.", "瓶子里装满了水。"),
            Word(38, "cup", "杯子", "/kʌp/", "I drink coffee from this cup.", "我用这个杯子喝咖啡。"),
            Word(39, "plate", "盘子", "/pleɪt/", "Put the food on the plate.", "把食物放在盘子里。"),
            Word(40, "knife", "刀", "/naɪf/", "Be careful with the knife.", "小心使用刀子。"),

            // 食物类 (41-60)
            Word(41, "bread", "面包", "/bred/", "I eat bread for breakfast.", "我早餐吃面包。"),
            Word(42, "milk", "牛奶", "/mɪlk/", "Children need to drink milk.", "孩子们需要喝牛奶。"),
            Word(43, "egg", "鸡蛋", "/eɡ/", "I cook an egg for lunch.", "我午餐煮个鸡蛋。"),
            Word(44, "fish", "鱼", "/fɪʃ/", "Fish is good for health.", "鱼对健康有益。"),
            Word(45, "meat", "肉", "/miːt/", "I don't eat much meat.", "我不怎么吃肉。"),
            Word(46, "rice", "米饭", "/raɪs/", "Rice is a staple food.", "米饭是主食。"),
            Word(47, "vegetable", "蔬菜", "/ˈvedʒtəbl/", "Eat more vegetables.", "多吃蔬菜。"),
            Word(48, "fruit", "水果", "/fruːt/", "Fruit contains vitamins.", "水果含有维生素。"),
            Word(49, "banana", "香蕉", "/bəˈnænə/", "Bananas are yellow.", "香蕉是黄色的。"),
            Word(50, "orange", "橙子", "/ˈɔːrɪndʒ/", "This orange is sweet.", "这个橙子很甜。"),
            Word(51, "grape", "葡萄", "/ɡreɪp/", "I like purple grapes.", "我喜欢紫葡萄。"),
            Word(52, "strawberry", "草莓", "/ˈstrɔːberi/", "Strawberries are red.", "草莓是红色的。"),
            Word(53, "tomato", "西红柿", "/təˈmeɪtoʊ/", "Tomatoes are used in salad.", "西红柿用来做沙拉。"),
            Word(54, "potato", "土豆", "/pəˈteɪtoʊ/", "I like fried potatoes.", "我喜欢炸土豆。"),
            Word(55, "carrot", "胡萝卜", "/ˈkærət/", "Carrots are good for eyes.", "胡萝卜对眼睛有好处。"),
            Word(56, "onion", "洋葱", "/ˈʌnjən/", "Onions make me cry.", "洋葱让我流泪。"),
            Word(57, "cheese", "奶酪", "/tʃiːz/", "I put cheese on pizza.", "我在披萨上放奶酪。"),
            Word(58, "butter", "黄油", "/ˈbʌtər/", "Spread butter on bread.", "在面包上涂黄油。"),
            Word(59, "sugar", "糖", "/ˈʃʊɡər/", "Too much sugar is bad.", "糖吃太多不好。"),
            Word(60, "salt", "盐", "/sɔːlt/", "Add some salt to the soup.", "在汤里加点盐。"),

            // 颜色和形状 (61-80)
            Word(61, "red", "红色", "/red/", "The rose is red.", "玫瑰是红色的。"),
            Word(62, "blue", "蓝色", "/bluː/", "The sky is blue.", "天空是蓝色的。"),
            Word(63, "green", "绿色", "/ɡriːn/", "Grass is green.", "草是绿色的。"),
            Word(64, "yellow", "黄色", "/ˈjeloʊ/", "The sun looks yellow.", "太阳看起来是黄色的。"),
            Word(65, "black", "黑色", "/blæk/", "I wear black shoes.", "我穿黑色鞋子。"),
            Word(66, "white", "白色", "/waɪt/", "Snow is white.", "雪是白色的。"),
            Word(67, "purple", "紫色", "/ˈpɜːrpl/", "I like purple flowers.", "我喜欢紫色的花。"),
            Word(68, "pink", "粉色", "/pɪŋk/", "She wears a pink dress.", "她穿粉色裙子。"),
            Word(69, "brown", "棕色", "/braʊn/", "The bear is brown.", "熊是棕色的。"),
            Word(70, "gray", "灰色", "/ɡreɪ/", "The clouds are gray.", "云朵是灰色的。"),
            Word(71, "circle", "圆形", "/ˈsɜːrkl/", "Draw a circle here.", "在这里画个圆。"),
            Word(72, "square", "正方形", "/skwer/", "The box is square.", "盒子是正方形的。"),
            Word(73, "triangle", "三角形", "/ˈtraɪæŋɡl/", "A triangle has three sides.", "三角形有三条边。"),
            Word(74, "rectangle", "长方形", "/ˈrektæŋɡl/", "The paper is rectangle.", "纸是长方形的。"),
            Word(75, "star", "星形", "/stɑːr/", "I see a bright star.", "我看到一颗亮星。"),
            Word(76, "heart", "心形", "/hɑːrt/", "Draw a red heart.", "画一个红心。"),
            Word(77, "line", "线", "/laɪn/", "Draw a straight line.", "画一条直线。"),
            Word(78, "point", "点", "/pɔɪnt/", "Put a point here.", "在这里点一个点。"),
            Word(79, "corner", "角", "/ˈkɔːrnər/", "Stand in the corner.", "站在角落里。"),
            Word(80, "edge", "边缘", "/edʒ/", "Be careful of the edge.", "小心边缘。"),

            // 动作词汇 (81-100)
            Word(81, "run", "跑", "/rʌn/", "I run every morning.", "我每天早上跑步。"),
            Word(82, "walk", "走", "/wɔːk/", "Let's walk to the park.", "我们走路去公园。"),
            Word(83, "jump", "跳", "/dʒʌmp/", "The cat can jump high.", "猫能跳得很高。"),
            Word(84, "swim", "游泳", "/swɪm/", "I swim in the pool.", "我在游泳池游泳。"),
            Word(85, "fly", "飞", "/flaɪ/", "Birds can fly in the sky.", "鸟儿能在天空飞翔。"),
            Word(86, "sing", "唱歌", "/sɪŋ/", "She sings beautifully.", "她唱歌很好听。"),
            Word(87, "dance", "跳舞", "/dæns/", "They dance at the party.", "他们在聚会上跳舞。"),
            Word(88, "read", "读", "/riːd/", "I read books every day.", "我每天读书。"),
            Word(89, "write", "写", "/raɪt/", "Please write your name.", "请写下你的名字。"),
            Word(90, "listen", "听", "/ˈlɪsn/", "Listen to the music.", "听音乐。"),
            Word(91, "speak", "说话", "/spiːk/", "Can you speak English?", "你会说英语吗？"),
            Word(92, "eat", "吃", "/iːt/", "It's time to eat dinner.", "该吃晚饭了。"),
            Word(93, "drink", "喝", "/drɪŋk/", "Drink more water.", "多喝水。"),
            Word(94, "sleep", "睡觉", "/sliːp/", "I sleep eight hours.", "我睡八小时。"),
            Word(95, "work", "工作", "/wɜːrk/", "I work in an office.", "我在办公室工作。"),
            Word(96, "play", "玩", "/pleɪ/", "Children like to play.", "孩子们喜欢玩。"),
            Word(97, "study", "学习", "/ˈstʌdi/", "I study English hard.", "我努力学习英语。"),
            Word(98, "teach", "教", "/tiːtʃ/", "She teaches math.", "她教数学。"),
            Word(99, "learn", "学", "/lɜːrn/", "We learn something new.", "我们学习新东西。"),
            Word(100, "help", "帮助", "/help/", "Can you help me?", "你能帮助我吗？")
        ))
    }
    
    fun getAllWords(): List<Word> = words.toList()
    
    fun getUnlearnedWords(): List<Word> = words.filter { !it.isLearned }
    
    fun getLearnedWords(): List<Word> = words.filter { it.isLearned }
    
    fun getRandomWord(): Word? = getUnlearnedWords().randomOrNull()
    
    fun markWordAsLearned(wordId: Int) {
        words.find { it.id == wordId }?.let { word ->
            word.isLearned = true
            word.reviewCount++
            word.lastReviewTime = System.currentTimeMillis()
        }
    }
    
    fun markWordAsUnlearned(wordId: Int) {
        words.find { it.id == wordId }?.isLearned = false
    }
    
    fun getWordById(id: Int): Word? = words.find { it.id == id }
}