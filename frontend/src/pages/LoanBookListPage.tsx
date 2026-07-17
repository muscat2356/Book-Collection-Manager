import { useCallback, useEffect, useState } from "react"
import { useApiClient } from "../api/ApiClientContext"
import { fetchActiveLoans, type ActiveLoan } from "../api/loans"
import { LoanCard } from "../components/LoanCard"

export function LoanBookListPage() {
  const apiClient = useApiClient()
  const [activeLoans, setActiveLoans] = useState<ActiveLoan[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // 初回取得と「返却後の再取得」で同じ処理を使う
  const loadLoans = useCallback(() => {
    setLoading(true)
    setError(null)
    fetchActiveLoans(apiClient)
      .then((data) => setActiveLoans(data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [apiClient])

  useEffect(() => {
    loadLoans()
  }, [loadLoans])

  if (loading) {
    return <p className="page-status">読み込み中…</p>
  }

  if (error) {
    return <p className="page-status page-status--error">{error}</p>
  }

  return (
    <section className="page">
      <h1>貸出中一覧</h1>
      <p className="page__lead">貸出中の本の一覧です</p>
      {activeLoans.length === 0 ? (
        <p className="page__lead">現在貸出中の本はありません</p>
      ) : (
        <section className="book-list">
          {activeLoans.map((loan) => (
            <LoanCard key={loan.id} loan={loan} onReturned={loadLoans} />
          ))}
        </section>
      )}
    </section>
  )
}
