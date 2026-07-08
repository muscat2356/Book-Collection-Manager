
import { Link } from "react-router-dom"
import type { User } from "../types/User"

type UserCardProps = {
    user : User
}

export function UserCard({ user } : UserCardProps) {
    return(
        <article className="book-card">
            <div className="book-card__body">
                <div className="book-card__header">
                    <h2 className="book-card__title">{user.displayName}</h2>
                </div>
                <dl className="book-card__meta">
                    <dt>メールアドレス</dt>
                    <dd>メールアドレス：{user.email}</dd>
                </dl>
                <Link to={`/users/${user.id}/edit`} className="book-card__link">
                    編集
                </Link>
            </div>
        </article>
    )
}
