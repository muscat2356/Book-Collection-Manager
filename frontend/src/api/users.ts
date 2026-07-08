import { mockUsers } from "../data/mockUsers"
import type { User } from "../types/User";

export type CreateUserRequest = {
    displayName: string
    email: string
    temporaryPassword: string
}

export type UpdateUserRequest = {
    displayName: string
    email: string
}

export async function fetchUsers() {
    //APIの代わりの
    await new Promise((resolve) => setTimeout(resolve, 500))
    return mockUsers;
}

export async function fetchUserById(id:string):Promise<User | null> {
    const user = mockUsers.find((user) => user.id === id)
    return user ?? null;
}

export async function createUser(data: CreateUserRequest): Promise<User> {
    await new Promise((r) => setTimeout(r, 500))

    const newUser: User = {
        id: String(mockUsers.length + 1),
        keycloakSub: String(mockUsers.length + 1),
        displayName: data.displayName,
        email: data.email,
        isActive: true,
    }

    mockUsers.push(newUser)
    return newUser
}

export async function updateUser(id: string, data: UpdateUserRequest): Promise<User> {
    await new Promise((r) => setTimeout(r, 500))

    const index = mockUsers.findIndex((u) => u.id === id)
    if(index === -1){
        throw new Error('利用者が見つかりません')
    }

    mockUsers[index] = {
        ...mockUsers[index],
        displayName: data.displayName,
        email: data.email,
    }

    return mockUsers[index]
    
}
