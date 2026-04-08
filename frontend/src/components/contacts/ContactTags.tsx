import React, { useCallback }from 'react'
import { Badge } from '../ui/badge'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from '../ui/dropdown-menu'
import { Button } from '../ui/Button'
import { Plus, X } from 'lucide-react'
import { ScrollArea } from '../ui/scroll-area'
import { mockAvailableTags } from '../../constants/mocks'
import type { Tag } from '../../types/contact.types'

interface Props {
    contact: any
    onAddTag?: (tag: string) => void
    onRemoveTag?: (tag: string) => void
}

const ContactTags = ({ contact }: Props) => {

    const [selectedContact, setSelectedContact] = React.useState(contact)
    const [isAddTagOpen, setIsAddTagOpen] = React.useState(false)
    
 /*    const onAddTag = useCallback((tag: string) => {
        if (!selectedContact) return

        setContacts((prev) =>
            prev.map((c) =>
                c.id === selectedContact.id
                    ? { ...c, tags: [...c.tags, tag] }
                    : c
            )
        )
        setSelectedContact((prev) =>
            prev ? { ...prev, tags: [...prev.tags, tag] } : null
        )
    }, [contact]) */

    const availableTagsFiltered = mockAvailableTags.filter(
        (tag) => !contact.tags.includes(tag)
    )

   /*  const handleAddTag = (tag: { id: number; name: string }) => {
        if (!contact.tags.includes(tag.id)) {
            onAddTag?.(tag)
        }
        setIsAddTagOpen(false)
    }
 */

    return (
        <div className="space-y-2.5">
            <label className="text-sm font-semibold text-foreground">
                Tags
            </label>
            <div className="flex flex-wrap gap-2.5 mt-3">
                {contact.tags.map((tag: Tag) => (
                    <Badge
                        key={tag.id}
                        className="bg-neutro-2/50 flex items-center gap-1"
                    >
                        {tag.name}
                        <X
                            className="h-3 w-3 cursor-pointer"
                            onClick={() => {}}
                        />
                    </Badge>
                ))}

                <DropdownMenu open={isAddTagOpen} onOpenChange={setIsAddTagOpen}>
                    <DropdownMenuTrigger /* asChild */>
                        <button
                            className="flex h-6 gap-1 text-xs"
                        >
                            <Plus className="h-3 w-3" />
                            Etiqueta
                        </button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="start" className="w-48">
                        <DropdownMenuLabel>Available Tags</DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        <ScrollArea className="h-48">
                            {availableTagsFiltered.map((tag) => (
                                <DropdownMenuItem
                                    key={tag}
                                    onClick={() => {/* handleAddTag({ id: tag.id, name: tag.name }) */}}
                                >
                                    {tag}
                                </DropdownMenuItem>
                            ))}
                            {availableTagsFiltered.length === 0 && (
                                <p className="px-2 py-4 text-center text-sm text-muted-foreground">
                                    All tags applied
                                </p>
                            )}
                        </ScrollArea>
                    </DropdownMenuContent>
                </DropdownMenu>
            </div>
        </div>
    )
}

export default ContactTags
