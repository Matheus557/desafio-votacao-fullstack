import { type FormEvent, type ReactNode, useEffect, useState } from 'react'
import {
  abrirPauta,
  cadastrarPauta,
  listarPautas,
  type Pauta,
  registrarVoto,
} from './api'
import './App.css'

function App() {
  const [pautas, setPautas] = useState<Pauta[]>([])
  const [pautaSelecionada, setPautaSelecionada] = useState<Pauta | null>(null)
  const [modalCadastroAberto, setModalCadastroAberto] = useState(false)
  const [pautaParaAbrir, setPautaParaAbrir] = useState<Pauta | null>(null)
  const [modalAviso, setModalAviso] = useState('')
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(true)

  async function carregarPautas() {
    setErro('')
    setCarregando(true)
    try {
      const response = await listarPautas()
      setPautas(response)
    } catch {
      setErro('Nao foi possivel carregar as pautas.')
    } finally {
      setCarregando(false)
    }
  }

  useEffect(() => {
    void carregarPautas()
  }, [])

  function voltarParaHome() {
    setPautaSelecionada(null)
    void carregarPautas()
  }

  if (pautaSelecionada) {
    return (
      <TelaVotacao
        pauta={pautaSelecionada}
        onVoltar={voltarParaHome}
      />
    )
  }

  return (
    <main className="home-page">
      <header className="topbar">
        <div>
          <p className="eyebrow">Assembleia digital</p>
          <h1>Pautas</h1>
        </div>
        <button
          className="primary-button"
          type="button"
          onClick={() => setModalCadastroAberto(true)}
        >
          Cadastrar nova pauta
        </button>
      </header>

      {erro && <p className="form-error">{erro}</p>}
      {carregando ? <p className="empty-state">Carregando pautas...</p> : null}

      <section className="pauta-list" aria-label="Lista de pautas">
        {!carregando && pautas.length === 0 ? (
          <p className="empty-state">Nenhuma pauta cadastrada.</p>
        ) : null}

        {pautas.map((pauta) => (
          <article className="pauta-card" key={pauta.id}>
            <div className="pauta-card-header">
              <div>
                <span className={pauta.aberta ? 'status open' : pauta.status === 'ENCERRADA' ? 'status closed' : 'status'}>
                  {pauta.aberta ? 'Aberta' : pauta.status === 'ENCERRADA' ? 'Encerrada' : 'Fechada'}
                </span>
                <h2>{pauta.nome}</h2>
              </div>
              <strong>{pauta.totalVotos} votos</strong>
            </div>

            <p>{pauta.descricao}</p>

            <div className="vote-summary">
              <span>Sim: {pauta.votosSim}</span>
              <span>Nao: {pauta.votosNao}</span>
            </div>

            <div className="card-actions">
              <button
                className="secondary-button"
                type="button"
                disabled={pauta.status === 'ABERTA'}
                onClick={() => {
                  if (pauta.status === 'ENCERRADA') {
                    setModalAviso('Tempo da votação encerrado.')
                    return
                  }
                  setPautaParaAbrir(pauta)
                }}
              >
                Abrir votacao
              </button>
              <button
                className="primary-button"
                type="button"
                disabled={!pauta.aberta}
                onClick={() => setPautaSelecionada(pauta)}
              >
                Votar
              </button>
            </div>
          </article>
        ))}
      </section>

      {modalCadastroAberto ? (
        <Modal title="Cadastrar nova pauta" onClose={() => setModalCadastroAberto(false)}>
          <CadastroPautaForm
            onSuccess={() => {
              setModalCadastroAberto(false)
              void carregarPautas()
            }}
          />
        </Modal>
      ) : null}

      {pautaParaAbrir ? (
        <Modal title="Abrir pauta para votacao" onClose={() => setPautaParaAbrir(null)}>
          <AbrirPautaForm
            pauta={pautaParaAbrir}
            onSuccess={(pautaAtualizada) => {
              setPautaParaAbrir(null)
              setPautaSelecionada(pautaAtualizada)
            }}
            onError={(message) => {
              setPautaParaAbrir(null)
              setModalAviso(message)
              void carregarPautas()
            }}
          />
        </Modal>
      ) : null}

      {modalAviso ? (
        <Modal title="Pauta encerrada" onClose={() => setModalAviso('')}>
          <p>{modalAviso}</p>
          <button className="primary-button full-width" type="button" onClick={() => setModalAviso('')}>
            Entendi
          </button>
        </Modal>
      ) : null}

    </main>
  )
}

function CadastroPautaForm({ onSuccess }: { onSuccess: () => void }) {
  const [nome, setNome] = useState('')
  const [descricao, setDescricao] = useState('')
  const [tempo, setTempo] = useState(1)
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErro('')
    setEnviando(true)

    try {
      await cadastrarPauta({ nome, descricao, tempoAbertoPorMinutos: tempo })
      onSuccess()
    } catch {
      setErro('Nao foi possivel cadastrar a pauta.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className="form-stack" onSubmit={handleSubmit}>
      <label>
        Titulo
        <input value={nome} onChange={(event) => setNome(event.target.value)} required />
      </label>
      <label>
        Descricao
        <textarea
          value={descricao}
          onChange={(event) => setDescricao(event.target.value)}
          required
        />
      </label>
      <label>
        Tempo padrao em minutos
        <input
          type="number"
          min="1"
          value={tempo}
          onChange={(event) => setTempo(Number(event.target.value))}
          required
        />
      </label>
      {erro && <p className="form-error">{erro}</p>}
      <button className="primary-button" type="submit" disabled={enviando}>
        {enviando ? 'Salvando...' : 'Cadastrar pauta'}
      </button>
    </form>
  )
}

function AbrirPautaForm({
  pauta,
  onSuccess,
  onError,
}: {
  pauta: Pauta
  onSuccess: (pauta: Pauta) => void
  onError: (message: string) => void
}) {
  const [tempo, setTempo] = useState(pauta.tempoAbertoPorMinutos || 1)
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErro('')
    setEnviando(true)

    try {
      const pautaAberta = await abrirPauta(pauta.id, { tempoAbertoPorMinutos: tempo })
      onSuccess(pautaAberta)
    } catch (error) {
      const message = error instanceof Error ? error.message : ''
      if (message.includes('Tempo da votação encerrado')) {
        onError('Tempo da votação encerrado.')
        return
      }

      setErro(message || 'Nao foi possivel abrir a pauta.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className="form-stack" onSubmit={handleSubmit}>
      <p>{pauta.nome}</p>
      <label>
        Tempo aberta em minutos
        <input
          type="number"
          min="1"
          value={tempo}
          onChange={(event) => setTempo(Number(event.target.value))}
          required
        />
      </label>
      {erro && <p className="form-error">{erro}</p>}
      <button className="primary-button" type="submit" disabled={enviando}>
        {enviando ? 'Abrindo...' : 'Abrir pauta'}
      </button>
    </form>
  )
}

function TelaVotacao({
  pauta,
  onVoltar,
}: {
  pauta: Pauta
  onVoltar: () => void
}) {
  const [associateId, setAssociateId] = useState('')
  const [cpf, setCpf] = useState('')
  const [voto, setVoto] = useState<'YES' | 'NO' | ''>('')
  const [mensagem, setMensagem] = useState('')
  const [erro, setErro] = useState('')
  const [modalAviso, setModalAviso] = useState('')
  const [pautaBloqueada, setPautaBloqueada] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [agora, setAgora] = useState(() => Date.now())

  const prazoEncerramento = pauta.dataEncerramento
    ? new Date(pauta.dataEncerramento).getTime()
    : null
  const tempoEsgotado =
    pautaBloqueada || Boolean(prazoEncerramento && agora >= prazoEncerramento)
  const segundosRestantes = prazoEncerramento
    ? Math.max(0, Math.ceil((prazoEncerramento - agora) / 1000))
    : null
  const minutosRestantes = segundosRestantes
    ? Math.floor(segundosRestantes / 60)
    : 0
  const segundosNoMinuto = segundosRestantes ? segundosRestantes % 60 : 0

  useEffect(() => {
    if (!prazoEncerramento) {
      return
    }

    const intervalId = window.setInterval(() => {
      setAgora(Date.now())
    }, 1000)

    return () => window.clearInterval(intervalId)
  }, [prazoEncerramento])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setMensagem('')
    setErro('')

    if (!voto) {
      setErro('Selecione Sim ou Nao para registrar o voto.')
      return
    }

    const associateIdNumber = Number(associateId)
    if (!Number.isInteger(associateIdNumber) || associateIdNumber <= 0) {
      setErro('Informe um ID de associado valido.')
      return
    }

    if (tempoEsgotado) {
      setErro('Tempo esgotado. Esta pauta nao aceita novos votos.')
      return
    }

    setEnviando(true)

    try {
      await registrarVoto(pauta.id, { associateId: associateIdNumber, cpf, vote: voto })
      setMensagem('Voto registrado com sucesso.')
      setAssociateId('')
      setCpf('')
      setVoto('')
    } catch (error) {
      const message = error instanceof Error ? error.message : ''

      if (message.includes('Associado já votou')) {
        setModalAviso('Este associado ja votou nessa pauta e nao pode votar novamente.')
        return
      }

      if (message.includes('CPF inválido')) {
        setModalAviso('CPF invalido ou nao encontrado na validacao externa.')
        return
      }

      if (message.includes('não está habilitado')) {
        setModalAviso('Associado nao esta habilitado para votar.')
        return
      }

      if (message.includes('pauta não está mais aberta') || message.includes('Tempo da votação encerrado')) {
        setPautaBloqueada(true)
        setErro('Tempo da votação encerrado. Esta pauta nao aceita novos votos.')
        return
      }

      setErro(message || 'Nao foi possivel registrar o voto.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <main className="vote-page">
      <header className="topbar">
        <div>
          <p className="eyebrow">Pauta aberta</p>
          <h1>{pauta.nome}</h1>
        </div>
        <button className="secondary-button" type="button" onClick={onVoltar}>
          Fechar pauta
        </button>
      </header>

      <section className="vote-panel">
        <p>{pauta.descricao}</p>
        <div className={tempoEsgotado ? 'deadline expired' : 'deadline'}>
          {tempoEsgotado ? (
            <strong>Tempo da votação encerrado. Votacao bloqueada.</strong>
          ) : (
            <strong>
              Tempo restante: {minutosRestantes.toString().padStart(2, '0')}:
              {segundosNoMinuto.toString().padStart(2, '0')}
            </strong>
          )}
        </div>
        <form className="form-stack" onSubmit={handleSubmit}>
          <label>
            ID do associado
            <input
              type="number"
              min="1"
              value={associateId}
              onChange={(event) => setAssociateId(event.target.value)}
              inputMode="numeric"
              disabled={tempoEsgotado}
              required
            />
          </label>

          <label>
            CPF
            <input
              value={cpf}
              onChange={(event) => setCpf(event.target.value)}
              inputMode="numeric"
              disabled={tempoEsgotado}
              required
            />
          </label>

          <fieldset className="radio-group" disabled={tempoEsgotado}>
            <legend>Voto</legend>
            <label>
              <input
                type="radio"
                name="voto"
                value="YES"
                checked={voto === 'YES'}
                onChange={() => setVoto('YES')}
                required
              />
              Sim
            </label>
            <label>
              <input
                type="radio"
                name="voto"
                value="NO"
                checked={voto === 'NO'}
                onChange={() => setVoto('NO')}
              />
              Nao
            </label>
          </fieldset>

          {mensagem && <p className="form-success">{mensagem}</p>}
          {erro && <p className="form-error">{erro}</p>}

          <button className="primary-button" type="submit" disabled={enviando || tempoEsgotado}>
            {enviando ? 'Registrando...' : 'Registrar voto'}
          </button>
        </form>
      </section>

      {modalAviso ? (
        <Modal title="Voto nao registrado" onClose={() => setModalAviso('')}>
          <p>{modalAviso}</p>
          <button className="primary-button full-width" type="button" onClick={() => setModalAviso('')}>
            Entendi
          </button>
        </Modal>
      ) : null}
    </main>
  )
}

function Modal({
  title,
  children,
  onClose,
}: {
  title: string
  children: ReactNode
  onClose: () => void
}) {
  return (
    <div className="modal-backdrop" role="presentation">
      <section className="modal" role="dialog" aria-modal="true" aria-label={title}>
        <header className="modal-header">
          <h2>{title}</h2>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Fechar">
            x
          </button>
        </header>
        {children}
      </section>
    </div>
  )
}

export default App
