param(
    [Parameter(Mandatory)][uri]$BaseUrl,
    [Parameter(Mandatory)][ValidateSet('filme','endereco','cliente','funcionario','locacao')][string]$Entity
)

$ErrorActionPreference = 'Stop'
if (!$BaseUrl.IsLoopback) { throw 'Use a disposable local database and application.' }
$session = [Microsoft.PowerShell.Commands.WebRequestSession]::new()
$base = $BaseUrl.AbsoluteUri.TrimEnd('/')
$route = $base + @{ filme='/filmes'; endereco='/enderecos'; cliente='/clientes'; funcionario='/funcionarios'; locacao='/locacoes' }[$Entity]

function Invoke-Crud([string]$Query = '', [hashtable]$Body = $null, [int]$Status = 200) {
    $options = @{ Uri = "$route$Query"; WebSession = $session; SkipHttpErrorCheck = $true }
    if ($null -ne $Body) {
        $options.Method = 'Post'
        $options.Body = $Body
    }
    $response = Invoke-WebRequest @options
    if ($response.StatusCode -ne $Status) {
        throw "Expected HTTP $Status for $Query, got $($response.StatusCode)"
    }
    return [string]$response.Content
}

function Get-Hidden([string]$Html, [string]$Name) {
    $match = [regex]::Match($Html, 'name="' + $Name + '" value="([^"]+)"')
    if (!$match.Success) { throw "Missing form field $Name" }
    return $match.Groups[1].Value
}

if ($Entity -eq 'locacao') {
    $fixture = 'Locacao-' + [guid]::NewGuid().ToString('N')
    $route = "$base/clientes"
    $csrf = Get-Hidden (Invoke-Crud '?acao=novo') 'csrf'
    $null = Invoke-Crud -Body @{
        acao='inserir'; csrf=$csrf; nome=$fixture; documento=$fixture.Substring(0,24)
        logradouro=$fixture; ibge='0123456'; cep='00123456'
    }
    $clientId = [regex]::Match((Invoke-Crud "?q=$fixture"), 'acao=editar&amp;id=(\d+)').Groups[1].Value
    $route = "$base/filmes"
    $films = @()
    foreach ($index in 1,2) {
        $title = "$fixture-$index"
        $null = Invoke-Crud -Body @{ acao='inserir'; csrf=$csrf; titulo=$title; nota_imb='6'; valor_dia='2' }
        $films += [regex]::Match((Invoke-Crud "?q=$title"), 'acao=editar&amp;id=(\d+)').Groups[1].Value
    }
    if (!$clientId -or !$films[0] -or !$films[1]) { throw 'Rental fixtures missing' }
    $route = "$base/locacoes"
    $data = @{ acao='inserir'; csrf=$csrf; cliente_id=$clientId; qtd_dias='2'; filmes=($films -join ', '); data_devolucao='' }
    $null = Invoke-Crud -Body $data
    $list = Invoke-Crud "?q=$fixture-1"
    $id = [regex]::Match($list, 'acao=editar&amp;id=(\d+)').Groups[1].Value
    if (!$id -or !$list.Contains("$fixture-2")) { throw 'Search by one film omitted another rental film' }
    $edit = Invoke-Crud "?acao=editar&id=$id"
    if (!$edit.Contains("value=`"$($data.filmes)`"")) { throw 'Rental film codes did not prefill' }
    $data.acao = 'alterar'
    $data.id = $id
    $data.qtd_dias = '4'
    $null = Invoke-Crud -Body $data
    $invalid = $data.Clone()
    $invalid.qtd_dias = '7'
    $invalid.filmes = "$($films[0]), $($films[0])"
    $null = Invoke-Crud -Body $invalid -Status 400
    $invalid.filmes = "$($films[0]), 2147483647"
    $null = Invoke-Crud -Body $invalid -Status 409
    $edit = Invoke-Crud "?acao=editar&id=$id"
    if (!$edit.Contains('step="1" value="4"') -or !$edit.Contains("value=`"$($data.filmes)`"")) {
        throw 'Failed edit did not roll back the rental and its film links'
    }
    $route = "$base/filmes"
    $null = Invoke-Crud -Body @{ acao='excluir'; id=$films[0]; csrf=$csrf } -Status 409
    $route = "$base/locacoes"
    $null = Invoke-Crud "?acao=confirmar-exclusao&id=$id"
    $null = Invoke-Crud -Body @{ acao='excluir'; id=$id; csrf=$csrf }
    $null = Invoke-Crud "?acao=editar&id=$id" -Status 404
    $route = "$base/filmes"
    foreach ($filmId in $films) {
        $null = Invoke-Crud "?acao=editar&id=$filmId"
        $null = Invoke-Crud -Body @{ acao='excluir'; id=$filmId; csrf=$csrf }
    }
    $route = "$base/clientes"
    $null = Invoke-Crud "?acao=editar&id=$clientId"
    if ($env:CRUD_HISTORICAL_RENTAL) {
        $oldId, $oldClient, $oldFilm = $env:CRUD_HISTORICAL_RENTAL.Split(',')
        $route = "$base/locacoes"
        $data = @{ acao='alterar'; csrf=$csrf; id=$oldId; cliente_id=$oldClient; qtd_dias='4'; filmes=$oldFilm; data_devolucao='2001-02-02' }
        $null = Invoke-Crud -Body $data -Status 400
        $data.data_devolucao = '2001-02-04'
        $null = Invoke-Crud -Body $data
        $edit = Invoke-Crud "?acao=editar&id=$oldId"
        if (!$edit.Contains('min="2001-02-03"') -or !$edit.Contains('value="2001-02-04"')) { throw 'Historical dates did not round-trip' }
        $data.data_devolucao = ''
        $null = Invoke-Crud -Body $data
        $edit = Invoke-Crud "?acao=editar&id=$oldId"
        if (!$edit.Contains('min="2001-02-03" max="9999-12-31" value=""')) { throw 'Return date was not cleared' }
    }
    Write-Output "CRUD smoke passed for $Entity"
    return
}

if ($Entity -in 'cliente','funcionario') {
    $employee = $Entity -eq 'funcionario'
    $fixture = $Entity + '-' + [guid]::NewGuid().ToString('N')
    $doc = [guid]::NewGuid().ToString('N').Substring(0,24)
    $csrf = Get-Hidden (Invoke-Crud '?acao=novo') 'csrf'
    $data = @{ acao='inserir'; csrf=$csrf; nome=$fixture; documento=$doc; logradouro=$fixture; ibge='0123456'; cep='00123456' }
    if ($employee) { $data.cargo='Atendente'; $data.nivel_acesso='2' }
    $null = Invoke-Crud -Body $data
    $list = Invoke-Crud "?q=$doc"
    $found = [regex]::Match($list, 'acao=editar&amp;id=(\d+)')
    if (!$found.Success -or !$list.Contains($fixture)) { throw 'Client creation or document search failed' }
    $id = $found.Groups[1].Value
    $copy = $data.Clone()
    $copy.logradouro = "Duplicate-$fixture"
    $null = Invoke-Crud -Body $copy -Status 409
    $addresses = Invoke-WebRequest -Uri "$base/enderecos?q=$($copy.logradouro)"
    if ($addresses.Content.Contains("<td>$($copy.logradouro)</td>")) { throw 'Duplicate document created a stray address' }
    $edit = Invoke-Crud "?acao=editar&id=$id"
    if (!$edit.Contains("value=`"$fixture`"") -or !$edit.Contains('Editar endereço')) { throw 'Client edit did not prefill linked data' }
    $update = @{ acao='alterar'; csrf=$csrf; id=$id; nome="$fixture atualizado"; documento=$doc }
    if ($employee) {
        if (!$edit.Contains('value="Atendente"') -or !$edit.Contains('value="2"')) { throw 'Employee fields did not prefill' }
        $update.cargo='Gerencia'
        $update.nivel_acesso='-1'
        $null = Invoke-Crud -Body $update -Status 400
        if (!(Invoke-Crud "?acao=editar&id=$id").Contains('value="Atendente"')) { throw 'Invalid employee edit changed cargo' }
        $update.nivel_acesso='3'
    }
    $null = Invoke-Crud -Body $update
    if ($employee -and !(Invoke-Crud "?acao=editar&id=$id").Contains('value="Gerencia"')) { throw 'Employee cargo was not updated' }
    if (!(Invoke-Crud "?acao=confirmar-exclusao&id=$id").Contains($update.nome)) { throw 'Client update failed' }
    $null = Invoke-Crud -Body @{ acao='excluir'; id=$id; csrf=$csrf }
    $null = Invoke-Crud "?acao=editar&id=$id" -Status 404
    $addresses = Invoke-WebRequest -Uri "$base/enderecos?q=$fixture"
    if (!$addresses.Content.Contains("<td>$fixture</td>")) { throw 'Client delete removed its address' }
    if ($env:CRUD_LEGACY_DOCUMENT) {
        $legacy = Invoke-Crud "?q=$env:CRUD_LEGACY_DOCUMENT"
        $legacyId = Get-Hidden (Invoke-Crud ('?acao=editar&id=' + [regex]::Match($legacy, 'acao=editar&amp;id=(\d+)').Groups[1].Value)) 'id'
        $data = @{ acao='alterar'; csrf=$csrf; id=$legacyId; nome='Cliente legado atualizado'; documento=$env:CRUD_LEGACY_DOCUMENT }
        if ($employee) { $data.nivel_acesso='0' }
        $null = Invoke-Crud -Body $data
        $legacy = Invoke-Crud "?q=$env:CRUD_LEGACY_DOCUMENT"
        if (!$legacy.Contains('Sem endereço') -or !$legacy.Contains('2001-02-03')) { throw 'Legacy address or registration date was changed' }
        $data.logradouro = $fixture + '-legacy'
        $data.ibge = '0123456'
        $data.cep = '00123456'
        $null = Invoke-Crud -Body $data
        if (!(Invoke-Crud "?acao=editar&id=$legacyId").Contains('Editar endereço')) { throw 'Legacy client could not add an address' }
        if (!$employee) {
            $null = Invoke-Crud -Body @{ acao='excluir'; id=$legacyId; csrf=$csrf } -Status 409
            $null = Invoke-Crud "?acao=editar&id=$legacyId"
            $rentals = Invoke-WebRequest -Uri "$base/locacoes?q=Cliente%20legado%20atualizado"
            if (!$rentals.Content.Contains('Cliente legado atualizado')) { throw 'Rental linked to the client is missing' }
        }
    }
    Write-Output "CRUD smoke passed for $Entity"
    return
}

if ($Entity -eq 'endereco') {
    $fixture = 'Rua-' + [guid]::NewGuid().ToString('N')
    $csrf = Get-Hidden (Invoke-Crud '?acao=novo') 'csrf'
    $data = @{ acao='inserir'; csrf=$csrf; logradouro=$fixture; bairro='Centro'; numero='12'; ibge='0123456'; cep='00123456' }
    $null = Invoke-Crud -Body $data
    $list = Invoke-Crud "?q=$fixture"
    $found = [regex]::Match($list, 'acao=editar&amp;id=(\d+)')
    if (!$found.Success -or !$list.Contains('<td>0123456</td>') -or !$list.Contains('<td>00123456</td>')) {
        throw 'Address missing or leading zeros lost'
    }
    $id = $found.Groups[1].Value
    $edit = Invoke-Crud "?acao=editar&id=$id"
    if (!$edit.Contains('value="0123456"') -or !$edit.Contains('value="00123456"')) { throw 'Address edit did not prefill codes' }
    $data.acao = 'alterar'
    $data.id = $id
    $data.logradouro += ' atualizado'
    $null = Invoke-Crud -Body $data
    $confirmation = Invoke-Crud "?acao=confirmar-exclusao&id=$id"
    if (!$confirmation.Contains($data.logradouro) -or !(Invoke-Crud "?q=$fixture").Contains($data.logradouro)) {
        throw 'Confirmation did not preserve the updated address'
    }
    $null = Invoke-Crud -Body @{ acao='excluir'; id=$id; csrf=$csrf }
    $null = Invoke-Crud "?acao=editar&id=$id" -Status 404
    foreach ($tipo in 'cliente','funcionario') {
        $linked = "$fixture-$tipo"
        $person = @{ acao='inserir'; csrf=$csrf; nome=$linked; documento=[guid]::NewGuid().ToString('N').Substring(0,24)
            logradouro=$linked; bairro='Centro'; numero='1'; ibge='0123456'; cep='00123456'; nivel_acesso='0' }
        $peopleRoute = "$base/${tipo}s"
        $response = Invoke-WebRequest -Uri $peopleRoute -WebSession $session -Method Post -Body $person
        if (!$response.Content.Contains($linked)) { throw "Fixture did not create $tipo" }
        $found = [regex]::Match((Invoke-Crud "?q=$linked"), 'acao=editar&amp;id=(\d+)')
        if (!$found.Success) { throw 'Linked address fixture missing' }
        $null = Invoke-Crud -Body @{ acao='excluir'; id=$found.Groups[1].Value; csrf=$csrf } -Status 409
        if (!(Invoke-Crud "?q=$linked").Contains($linked)) { throw 'Linked address was removed' }
        $personList = Invoke-WebRequest -Uri "${peopleRoute}?q=$linked"
        if (!$personList.Content.Contains($linked)) { throw 'Linked person was removed' }
    }
    Write-Output "CRUD smoke passed for $Entity"
    return
}

$fixture = 'Filme-' + [guid]::NewGuid().ToString('N')
$title = "$fixture Á & B"
$form = Invoke-Crud '?acao=novo'
$csrf = Get-Hidden $form 'csrf'
$data = @{ acao='inserir'; csrf=$csrf; titulo=$title; genero='Teste'; nota_imb='7.1'; valor_dia='2.34'; observacoes='' }
$invalid = $data.Clone()
$invalid.csrf = 'invalid'
$null = Invoke-Crud -Body $invalid -Status 403
$invalid = $data.Clone()
$invalid.nota_imb = '10.1'
$null = Invoke-Crud -Body $invalid -Status 400
$list = Invoke-Crud "?q=$fixture"
if ($list.Contains('<td>' + $fixture)) { throw 'An invalid request inserted the film' }
$null = Invoke-Crud -Body $data
$list = Invoke-Crud "?q=$fixture"
$found = [regex]::Match($list, 'acao=editar&amp;id=(\d+)')
if (!$found.Success -or !$list.Contains("$fixture Á &amp; B")) { throw 'Film absent or incorrectly escaped' }
$id = $found.Groups[1].Value
$edit = Invoke-Crud "?acao=editar&id=$id"
if (!$edit.Contains("value=`"$fixture Á &amp; B`"")) { throw 'Edit form did not prefill the title' }
$data.acao = 'alterar'
$data.id = $id
$data.titulo = "$fixture updated"
$null = Invoke-Crud -Body $data
$null = Invoke-Crud -Body $data
$confirmation = Invoke-Crud "?acao=confirmar-exclusao&id=$id"
if (!$confirmation.Contains($data.titulo)) { throw 'Confirmation did not show the updated title' }
$list = Invoke-Crud "?q=$fixture"
if (!$list.Contains($data.titulo)) { throw 'Confirmation GET removed the film' }
$delete = @{ acao='excluir'; id=$id; csrf=$csrf }
$null = Invoke-Crud -Body $delete
$null = Invoke-Crud "?acao=editar&id=$id" -Status 404
$null = Invoke-Crud -Body $delete -Status 404
$null = Invoke-Crud -Body $data -Status 404
Write-Output "CRUD smoke passed for $Entity"
