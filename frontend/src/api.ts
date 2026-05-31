export type Pauta = {
  id: number
  nome: string
  descricao: string
  tempoAbertoPorMinutos: number
  dataCriacao: string
  dataEncerramento: string | null
  status: 'FECHADA' | 'ABERTA' | 'ENCERRADA'
  aberta: boolean
  totalVotos: number
  votosSim: number
  votosNao: number
}

export type NovaPautaPayload = {
  nome: string
  descricao: string
  tempoAbertoPorMinutos: number
}

export type AbrirPautaPayload = {
  tempoAbertoPorMinutos: number
}

export type VotoPayload = {
  cpf: string
  pautaId: number
  voto: 'Sim' | 'Não'
}

const API_BASE = '/api'

async function request<T>(path: string, options: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || 'Nao foi possivel concluir a operacao.')
  }

  return response.json() as Promise<T>
}

export function listarPautas() {
  return request<Pauta[]>('/pautas/informacoes', {
    method: 'GET',
  })
}

export function cadastrarPauta(payload: NovaPautaPayload) {
  return request<Pauta>('/pautas/cadastro', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function abrirPauta(pautaId: number, payload: AbrirPautaPayload) {
  return request<Pauta>(`/pautas/${pautaId}/abrir`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export function registrarVoto(payload: VotoPayload) {
  return request('/votos/receber', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}
