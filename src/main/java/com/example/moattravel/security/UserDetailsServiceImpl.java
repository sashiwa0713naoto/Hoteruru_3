package com.example.moattravel.security; //Spring Securityでのログイン認証時に、入力されたメールアドレスをもとにデータベースからユーザー情報を検索し、保有ロールを含む認証用オブジェクト（UserDetailsImpl）を生成して提供するためのサービスクラス。

import java.util.ArrayList; // 可変長の配列リストを作成するためのクラス
import java.util.Collection; // 権限リストなどを保持するためのCollectionインターフェースをインポート

// Spring Securityが提供する権限管理・ユーザー検索用のクラスをインポート
import org.springframework.security.core.GrantedAuthority; // ユーザーに付与される権限を表すインターフェース
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 権限名を文字列から生成するためのクラス
import org.springframework.security.core.userdetails.UserDetails; // Spring Securityが認証・認可で使用するユーザー情報を表すインターフェース
import org.springframework.security.core.userdetails.UserDetailsService; // ログイン時のユーザー検索処理を行うためのインターフェース
import org.springframework.security.core.userdetails.UsernameNotFoundException; // ユーザーが見つからなかった場合にスローされる例外クラス
import org.springframework.stereotype.Service; // Springのサービス層のコンポーネントとしてDIコンテナに登録するためのアノテーション

import com.example.moattravel.entity.User; // ユーザー情報を保持するエンティティクラス
import com.example.moattravel.repository.UserRepository; // ユーザー情報のデータベース操作を行うリポジトリ

@Service // Springのコンポーネントスキャン対象（サービス層のBean）として登録
public class UserDetailsServiceImpl implements UserDetailsService { // Spring Securityのユーザー検索インターフェースを実装

    private final UserRepository userRepository; // DI（依存性注入）用リポジトリ

    // コンストラクタインジェクション（UserRepositoryを受け取る）
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ログイン時に入力されたメールアドレス（＝ユーザー名）を基にユーザー情報を検索・返却する核心メソッド
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            // DBからメールアドレスに一致するユーザーを取得
            User user = userRepository.findByEmail(email);
            
            // ユーザーに設定されているロール名（例: "ROLE_GENERAL", "ROLE_ADMIN"）を取得
            String userRoleName = user.getRole().getName();
            
            // Spring Securityが解釈できる権限リストオブジェクト（GrantedAuthority）を作成
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(userRoleName));
            
            // UserDetailsImplインスタンスにDBのUser情報と権限リストを渡して返却
            return new UserDetailsImpl(user, authorities);
        } catch (Exception e) {
            // ユーザーが見つからない（またはNullエラー等）場合、認証失敗用の例外を発行
            throw new UsernameNotFoundException("ユーザーが見つかりませんでした。");
        }
    }
}