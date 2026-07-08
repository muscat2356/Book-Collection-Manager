import type React from "react"
import { returnLoans, type ActiveLoan } from "../api/loans"
import { useState } from "react";

type LoanCardProps = {
    loan: ActiveLoan
    onReturned: () => void
}

export function LoanCard({ loan, onReturned } : LoanCardProps) {

    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    function handleReturn(e: React.MouseEvent<HTMLButtonElement>){
        e.preventDefault()
        setError(null)
        setSubmitting(true)

        returnLoans(loan.id)
        .then(() => onReturned())
        .catch((err) => setError(err.message))
        .finally(() => setSubmitting(false))

    }

    return(
        <article className="book-card">
            <div className="book-card__body">
                <div className="book-card__header">
                    <h2 className="book-card__title">{loan.book.title}</h2>
                </div>
                <dl className="book-card__meta">
                    <dt>著者</dt>
                    <dd>著者：{loan.book.author}</dd>
                    <dt>利用者</dt>
                    <dd>利用者：{loan.user.displayName}</dd>
                    <dt>貸出日</dt>
                    <dd>貸出日：{loan.borrowedAt}</dd>
                </dl>
            </div>

            <div className="book-card__actions">
                <button
                 className="btn btn--secondary"
                 onClick={handleReturn}
                 disabled={submitting}>
                    {submitting ? '処理中...' : '返却'}
                </button>
            </div>

            {error && (
                  <p className="page-status page-status--error">{error}</p>
            )}            

        </article>
    )
}
