/** GET /api/categories/tree のノード（大・中・小） */
export type CategoryNode = {
    id: number
    name: string
    children?: CategoryNode[]
  }
