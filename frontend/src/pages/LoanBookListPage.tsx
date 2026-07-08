import { useEffect, useState } from "react"
import { fetchActiveLoans, type ActiveLoan } from "../api/loans"
import { LoanCard } from "../components/LoanCard"

export function LoanBookListPage(){
  const [activeLoan, setActiveLoan] = useState<ActiveLoan[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchActiveLoans()
    .then((data) => setActiveLoan(data))
    .catch((err) => setError(err.message))
    .finally(() => setLoading(false))
  }, [])

  useEffect(() => { loadLoans() }, [])

  function loadLoans(){
    setLoading(true)
    fetchActiveLoans()
    .then(setActiveLoan)
    .catch((err) => setError(err.message))
    .finally(() => setLoading(false))
  }

  if(loading){
    return <p className="page-status">読み込み中・・・</p>
  }

  if(error) {
      return <p className="page-status page-status--error">{error}</p>
  }

    return(
        <section className="page">
        <h1>貸出中一覧</h1>
        <p className="page__lead">貸出中の本の一覧です</p>
          <section className="book-list">
            {activeLoan.map((loan) => 
            <LoanCard key={loan.id} loan={loan} onReturned={loadLoans} />)}
          </section>
      </section>
    )
}
