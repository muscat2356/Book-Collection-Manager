import { useRef, useState } from "react"
import { Link, Navigate, useNavigate } from "react-router-dom"
import { useApiClient } from "../api/ApiClientContext"
import { createLoan, LoanError } from "../api/loans"
import { useLoanCheckout } from "../loans/LoanContext"

export function CheckoutConfirmPage() {
  const apiClient = useApiClient()
  const navigate = useNavigate()
  const { user, selectedBookCopies, removeCopy, clearAll } = useLoanCheckout()
  const submittingRef = useRef(false);

  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Layout でも user ガード済みだが、選択ゼロは一覧へ
  if (!user) {
    return <Navigate to="/loans/checkout" replace />
  }
  if (selectedBookCopies.length === 0) {
    return <Navigate to="/loans/checkout/books" replace />
  }

  async function handleSubmit() {
    if (!user || selectedBookCopies.length === 0) return
    if (submittingRef.current) return
    submittingRef.current = true
    setSubmitting(true)
    setError(null)
    try {
      await createLoan(apiClient, {
        userId: user.id,
        bookCopyIds: selectedBookCopies.map((c) => c.bookCopyId),
      })
      clearAll()
      navigate("/loans/active")
    } catch (e) {
      // 409 で失敗した所蔵は選択から外す（残りは選び直せる状態にする）
      if (e instanceof LoanError) {
        if (e.errorCode === "BOOK_ALREADY_LOANED_BY_USER") {
          setError(
            `${e.message} 「選び直す」から別の所蔵を選んでください。`
          )
          return
        }
        if (e.failedBookCopyIds?.length) {
          e.failedBookCopyIds.forEach((id) => removeCopy(id))
        }
      }
      setError(e instanceof Error ? e.message : "貸出に失敗しました")    } finally {
      submittingRef.current = false
      setSubmitting(false)
    }
  }

  return (
    <section className="checkout-confirm">
      <h1>貸出内容の確認</h1>

      <p className="checkout-header__user">利用者: {user.displayName}</p>

      <h2>貸出する本</h2>
      <ol className="checkout-confirm__list">
        {selectedBookCopies.map((c, index) => (
          <li key={c.bookCopyId} className="checkout-confirm__item">
            <span>{index + 1}.</span>
            <span>
              {c.title} 所蔵 #{c.bookCopyId}
            </span>
          </li>
        ))}
      </ol>

      {error && <p className="page-status page-status--error">{error}</p>}

      <div className="checkout-confirm__actions">
        <Link to="/loans/checkout/books" className="btn">
          ← 選び直す
        </Link>
        <button
          type="button"
          className="btn btn--primary"
          disabled={submitting}
          onClick={handleSubmit}
        >
          {submitting ? "処理中…" : "貸出する"}
        </button>
      </div>
      <p className="page__lead">上記の内容で貸出処理を行います。</p>
    </section>
  )
}
