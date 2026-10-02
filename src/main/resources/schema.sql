<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>診断結果 | SAMURAI TRAVEL</title>
    <style>
        body {
            font-family: 'Helvetica Neue', Arial, sans-serif;
            background: linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%);
            color: #333;
            line-height: 1.7;
            padding: 20px;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0;
        }
        /* ローディング画面用 */
        #loading-screen {
            position: fixed;
            top: 0; left: 0; width: 100%; height: 100%;
            background: #ffffff;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            z-index: 9999;
            transition: opacity 0.5s ease;
        }
        .spinner {
            width: 50px; height: 50px;
            border: 5px solid #e2e8f0;
            border-top: 5px solid #007bff;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin-bottom: 20px;
        }
        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

        .container {
            max-width: 800px;
            width: 100%;
            background: #ffffff;
            padding: 40px;
            border-radius: 16px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            opacity: 0;
            transform: translateY(20px);
            animation: fadeIn 0.8s ease forwards 0.5s;
        }
        @keyframes fadeIn {
            to { opacity: 1; transform: translateY(0); }
        }
        .result-header {
            text-align: center;
            margin-bottom: 30px;
        }
        .result-badge {
            display: inline-block;
            background: #e0f2fe;
            color: #0369a1;
            padding: 6px 16px;
            border-radius: 20px;
            font-size: 14px;
            font-weight: bold;
            margin-bottom: 10px;
        }
        .destination-highlight {
            font-size: 32px;
            color: #007bff;
            font-weight: bold;
            margin: 10px 0;
        }
        .section-box {
            background: #f8fafc;
            border-left: 5px solid #007bff;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 25px;
        }
        .section-title {
            font-weight: bold;
            font-size: 18px;
            color: #1e293b;
            margin-bottom: 10px;
        }
        .hotel-card {
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 20px;
            background: #ffffff;
            margin-top: 20px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.02);
        }
        .hotel-name {
            font-size: 20px;
            font-weight: bold;
            color: #1e293b;
            margin-bottom: 8px;
        }
        .hotel-address {
            color: #64748b;
            font-size: 14px;
            margin-bottom: 12px;
        }
        .btn-primary {
            display: block;
            text-align: center;
            width: 100%;
            padding: 14px;
            background-color: #007bff;
            color: #ffffff;
            border-radius: 8px;
            text-decoration: none;
            font-weight: bold;
            font-size: 16px;
            margin-top: 30px;
            transition: background-color 0.2s;
            box-shadow: 0 4px 12px rgba(0,123,255,0.3);
            border: none;
            cursor: pointer;
        }
        .btn-primary:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>

<!-- ワクワク感を演出するローディング画面 -->
<div id="loading-screen">
    <div class="spinner"></div>
    <p style="font-weight: bold; color: #475569;">あなたの回答を分析し、運命の旅先を選定中...</p>
</div>

<!-- 判定結果のお宿情報がある場合のみ表示 -->
<div class="container" th:if="${house}">
    <div class="result-header">
        <span class="result-badge">診断完了 ✈️ 運命のデスティネーション</span>
        <p>あなたに最もおすすめの旅先は……</p>
        
        <!-- コントローラーから prefecture が渡されていない場合は汎用テキストを表示 -->
        <div class="destination-highlight" th:text="${prefecture} ?: 'あなたにぴったりのエリア'">あなたにぴったりのエリア</div>
    </div>

    <!-- おすすめの理由セクション -->
    <div class="section-box">
        <div class="section-title">🔍 なぜ、この場所があなたにおすすめなのか？</div>
        <!-- コントローラーから reason が渡されていない場合は汎用的な理由を表示 -->
        <p th:text="${reason} ?: 'アンケートの回答から、あなたの現在の気分や理想の過ごし方に最も近い環境を持つエリア・宿泊施設を厳選しました。日常から離れ、心地よい時間を過ごせるはずです。'">
            ここに理由が入ります。
        </p>
    </div>

    <!-- おすすめのお宿セクション（Java側の house オブジェクトと紐付け） -->
    <div class="section-box" style="border-left-color: #10b981;">
        <div class="section-title">🏨 あなたにぴったりのお宿</div>
        <div class="hotel-card">
            <!-- house.getName() や house.getAddress() で実際のDBデータを表示 -->
            <div class="hotel-name" th:text="${house.getName()}">宿名</div>
            <div class="hotel-address" th:text="'住所：' + ${house.getAddress()}">住所：...</div>
            <p th:text="${house.getDescription()}">お宿の説明文</p>
        </div>
    </div>

    <!-- 宿の詳細ページへ飛ぶリンク -->
    <a th:href="@{/houses/{id}(id=${house.getId()})}" class="btn-primary">このお宿の詳細を見る・予約する</a>
</div>

<!-- 万が一結果が取得できなかった場合のエラー画面 -->
<div class="container" th:unless="${house}">
    <div class="result-header">
        <h1 style="color: #ef4444;">結果が見つかりませんでした</h1>
        <p>恐れ入りますが、もう一度診断をやり直してください。</p>
        <a href="/diagnosis" class="btn-primary" style="background-color: #64748b; margin-top: 20px;">診断ページに戻る</a>
    </div>
</div>

<script>
    // 1.2秒後にローディング画面を消して結果をフェードイン
    window.addEventListener('load', function() {
        setTimeout(function() {
            const loader = document.getElementById('loading-screen');
            if (loader) {
                loader.style.opacity = '0';
                setTimeout(() => loader.style.display = 'none', 500);
            }
        }, 1200);
    });
</script>

</body>
</html>