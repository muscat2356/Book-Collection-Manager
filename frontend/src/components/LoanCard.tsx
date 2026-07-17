import { useState, type MouseEvent } from "react"
import { useApiClient } from "../api/ApiClientContext"
import { returnLoans, type ActiveLoan } from "../api/loans"

type LoanCardProps = {
  loan: ActiveLoan
  onReturned: () => void
}

export function LoanCard({ loan, onReturned }: LoanCardProps) {
  const apiClient = useApiClient()
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  function handleReturn(e: MouseEvent<HTMLButtonElement>) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)

    returnLoans(apiClient, loan.id)
      .then(() => onReturned())
      .catch((err) => setError(err.message))
      .finally(() => setSubmitting(false))
  }

  return (
    <article className="book-card">
      <div className="book-card__body">
        <div className="book-card__header">
          <h2 className="book-card__title">{loan.book.title}</h2>
        </div>
        <dl className="book-card__meta">
          <dt>著者</dt>
          <dd>著者：{loan.book.author}</dd>
          <dt>所蔵</dt>
          <dd>所蔵 #{loan.bookCopyId}</dd>
          <dt>利用者</dt>
          <dd>利用者：{loan.user.displayName}</dd>
          <dt>貸出日</dt>
          <dd>貸出日：{loan.borrowedAt}</dd>
        </dl>
      </div>

      <div className="book-card__actions">
        <button
          type="button"
          className="btn btn--secondary"
          onClick={handleReturn}
          disabled={submitting}
        >
          {submitting ? "処理中…" : "返却"}
        </button>
      </div>

      {error && (
        <p className="page-status page-status--error">{error}</p>
      )}
    </article>
  )
}
